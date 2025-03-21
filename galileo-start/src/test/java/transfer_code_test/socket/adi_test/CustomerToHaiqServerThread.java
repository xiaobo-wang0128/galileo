package transfer_code_test.socket.adi_test;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;
import org.armada.galileo.common.util.CommonUtil;
import org.armada.galileo.open.proxy.RunnableProxy;
import org.armada.galileo.open.util.ftp.FtpAuth;

import java.nio.charset.Charset;
import java.util.List;

/**
 * @author xiaobo
 * @date 2021/12/31 1:41 下午
 */
@Slf4j
public class CustomerToHaiqServerThread implements RunnableProxy {

    /* ------------ ftp 参数 ----------- */

    // ftp 参数
    private static final FtpAuth ftpAuth = new FtpAuth("10.10.0.55", 21, "adidas", "adidas741089");

    //ftp读取的目录
    private final String rootPath = "/ftp-data";


    /* ------------ tcp 参数 ----------- */
    // 端口号
    private static final int PORT = 8081;
    // 最大能接收的包大小
    private static final int Max_Package_size = 50 * 1024 * 1024;

    //private static final byte[] splitStart = new byte[]{0x02}, splitEnd = new byte[]{0x03};
    private static final byte[] splitStart = "<STX>".getBytes(), splitEnd = "<ETX>".getBytes();
    private static final String fileEndStr = "WAVEEND";

    private static final StringDecoder DECODER = new StringDecoder(Charset.forName("utf-8"));
    private static final StringEncoder ENCODER = new StringEncoder(Charset.forName("utf-8"));

    private static EventLoopGroup bossGroup = new NioEventLoopGroup(1);
    private static EventLoopGroup workerGroup = new NioEventLoopGroup();

    @Override
    public void run() {

        // 开启 ftp监听客户请求， 另起一个子线程
        //SdkProxyClassCache.startSubThread("customer_to_haiq", () -> {
        //   registerServerListener();
        // });

        //  开启tcp长连接监听客户请求
        doTcpListen();

    }

    @Override
    public void beforeDestroy() {

        log.info("线程关掉，开始回收资源");

        // 回收 sokcet 端口号、连接
        try {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        // 回收 ftp 连接
        ftpIsShutDown = true;
        try {
            // FtpConnectionPool.clearPool(ftpAuth);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    private void doTcpListen() {
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {

                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {

                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new AdiServerDecoder(splitStart, splitEnd));
//                            pipeline.addLast(DECODER);
//                            pipeline.addLast(ENCODER);

                            pipeline.addLast(new SimpleChannelInboundHandler<String>() {
                                @Override
                                public void channelRead0(ChannelHandlerContext ctx, String request) throws Exception {

                                    log.info("[tcp-server] 收到请求, ip: {}, request: {}", ctx.channel().remoteAddress(), request);

                                    if (CommonUtil.isEmpty(request)) {
                                        return;
                                    }
                                    try {
                                        // 在这里处理客户的请求
                                        // String resultJson = doTcpRequest(request);
                                        // 放入回调队列
                                        // HaiqToCustomerUtil.push(UUID.randomUUID().toString(), null, resultJson);
                                        ctx.channel().write(Unpooled.wrappedBuffer(splitStart));
                                        ctx.channel().write("resultJson");//"ok"
                                        ctx.channel().writeAndFlush(Unpooled.wrappedBuffer(splitEnd));

                                        log.info("[tcp-server] 响应请求, ip: {}, request: {}", ctx.channel().remoteAddress(), "");
                                    } catch (Exception e) {
                                        log.error(e.getMessage(), e);
                                    }
                                }

                                @Override
                                public void channelReadComplete(ChannelHandlerContext ctx) {
                                    ctx.flush();
                                }

                                @Override
                                public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
                                    ctx.close();
                                }

                                @Override
                                public void channelActive(ChannelHandlerContext ctx) throws Exception {
                                    super.channelActive(ctx);
                                    log.info("[tcp-server] 远程客户已连接: " + ctx.channel().remoteAddress());
                                }

                                @Override
                                public void channelInactive(ChannelHandlerContext ctx) throws Exception {
                                    super.channelInactive(ctx);
                                    log.info("[tcp-server] 远程客户已断开: " + ctx.channel().remoteAddress());
                                }
                            });
                        }
                    });

            b.bind(PORT).sync().channel().closeFuture().sync();

        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    class AdiServerDecoder extends ByteToMessageDecoder {

        private byte[] signHead;

        private byte[] signEnd;

        private boolean readHead = true;

        private boolean readEnd = false;

        public AdiServerDecoder(byte[] signHead, byte[] signEnd) {
            this.signHead = signHead;
            this.signEnd = signEnd;
        }

        int startIndex = -1;

        @Override
        protected void decode(ChannelHandlerContext ctx, ByteBuf buffer, List<Object> list) throws Exception {
            if (readHead) {
                startIndex = indexOf(buffer, signHead);
                if (startIndex >= 0) {
                    readEnd = true;
                    readHead = false;
                    buffer.skipBytes(signHead.length);
                }
            }

            if (readEnd) {
                int endIndex = indexOf(buffer, signEnd);
                if (endIndex >= 0) {
                    readEnd = false;
                    readHead = true;

                    ByteBuf frame = buffer.readRetainedSlice(endIndex - (startIndex + signHead.length));
                    buffer.skipBytes(signEnd.length);

                    list.add(frame);
                }
            }
        }

        private int indexOf(ByteBuf buffer, byte[] needle) {

            for (int i = buffer.readerIndex(); i < buffer.writerIndex(); i++) {
                for (int c = 0; c < needle.length; c++) {
                    int parentIndex = c + i;
                    if (parentIndex > buffer.writerIndex()) {
                        return -1;
                    }
                    if (buffer.getByte(parentIndex) != needle[c]) {
                        break;
                    }
                    // 对比到最后一个了
                    if (c == needle.length - 1) {
                        return i;
                    }
                }
            }
            return -1;
        }
    }

    private boolean ftpIsShutDown = false;

    public void registerServerListener() {
    }


    // --------------------------------------------------------------------------------------------------------------------------------
    // -----------------------------------------以上是网络通信处理方法， 以下是业务处理方法   -------------------------------------------------
    // --------------------------------------------------------------------------------------------------------------------------------


}