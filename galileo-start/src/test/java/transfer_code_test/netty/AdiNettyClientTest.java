package transfer_code_test.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.util.HashedWheelTimer;
import lombok.extern.slf4j.Slf4j;
import transfer_code_test.telnet.TelnetClientInitializer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author xiaobo
 * @date 2022/1/24 6:33 下午
 */
@Slf4j
public class AdiNettyClientTest {

//    static final String HOST = System.getProperty("host", "172.18.81.86");
//    static final int PORT = 7203;

    static final String HOST = System.getProperty("host", "127.0.0.1");
    static final int PORT = 33031;

    private static final int Max_Package_size = 50 * 1024 * 1024;
    private static final ByteBuf[] delimiter = new ByteBuf[]{Unpooled.wrappedBuffer(new byte[]{0x02}), Unpooled.wrappedBuffer(new byte[]{0x03})};
//    private static final ByteBuf[] delimiter = new ByteBuf[]{Unpooled.wrappedBuffer("<STX>".getBytes()), Unpooled.wrappedBuffer("<EXT>".getBytes())};

    private static final StringDecoder DECODER = new StringDecoder(Charset.forName("utf-8"));
    private static final StringEncoder ENCODER = new StringEncoder(Charset.forName("utf-8"));


    public static void main(String[] args) throws Exception {
        for (int i = 0; i < 1; i++) {
            new Thread(() -> {
                try {
                    doRequest();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }).start();
        }

        // Thread.sleep(Integer.MAX_VALUE);
    }


    private static EventLoopGroup group = new NioEventLoopGroup(Runtime.getRuntime().availableProcessors());


    public static void doRequest() throws Exception {

        CountDownLatch countDownLatch = new CountDownLatch(1);
        try {
            Bootstrap b = new Bootstrap();
            b.group(group).channel(NioSocketChannel.class).handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    log.info("已连接至远程服务器: {}", HOST);

                    ChannelPipeline pipeline = ch.pipeline();

                    // Add the text line codec combination first,
                    //pipeline.addLast(new DelimiterBasedFrameDecoder(10000000, Delimiters.lineDelimiter()));
                    pipeline.addLast(DECODER);
                    pipeline.addLast(ENCODER);

                    pipeline.addLast("readTimeOut", new ReadTimeoutHandler(3, TimeUnit.SECONDS));
                    pipeline.addLast(new SimpleChannelInboundHandler<String>() {
                        @Override
                        protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
                            System.err.println("response: " + msg);
                            // 在这里将 requestMessage status 设置成 success
                            //System.out.println("close here");
                            ctx.close();

                            countDownLatch.countDown();

                            System.out.println("channelRead0");
                        }

                        @Override
                        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                            if (cause instanceof io.netty.handler.timeout.ReadTimeoutException) {
                                log.info("[tcp-client] 读取 ack 超时,主 动断开连接, " + ctx.channel().remoteAddress());
                            } else {
                                log.info("[tcp-client] 异常主动断开连接, " + ctx.channel().remoteAddress());
                            }
                            ctx.close();

                            countDownLatch.countDown();

                            System.out.println("exceptionCaught");
                        }
                    });
                }
            });

            // Start the connection attempt.
            Channel ch = b.connect(HOST, PORT).sync().channel();

            ch.writeAndFlush("<STX>test: " + "abc" + "<ETX>");

            countDownLatch.await();

            System.out.println("done");

        } catch (Exception e) {

            log.error("socket_error: " + e.getMessage(), e);

            return;

        } finally {
            group.shutdownGracefully();
        }


    }
}
