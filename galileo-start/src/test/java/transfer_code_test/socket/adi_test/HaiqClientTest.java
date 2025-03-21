package transfer_code_test.socket.adi_test;

//package transfer_code_test.socket;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

/**
 * 接收 iwms-adatpor 的请求， 转发至客户的 socket server
 *
 * @author xiaobo
 * @date 2022/1/10 3:42 下午
 */
@Slf4j
public class HaiqClientTest {


    public static void main(String[] args) throws Exception {
        HaiqClientTest s = new HaiqClientTest();
        s.doTcpRequest("333", "127.0.0.1:8081", "<STX>abc,abc,abd<ETX>");
    }

    private void doTcpRequest(String requestId, String socketAddress, String inputString) throws Exception {

        String[] tmps = socketAddress.split(":");
        String host = tmps[0];
        int port = Integer.valueOf(tmps[1]);

        long start = System.currentTimeMillis();

        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group).channel(NioSocketChannel.class).handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    log.info("已连接至远程服务器: {}", socketAddress);
                    ChannelPipeline pipeline = ch.pipeline();

                    pipeline.addLast(new SimpleChannelInboundHandler<String>() {
                        @Override
                        public void channelRead0(ChannelHandlerContext ctx, String request) throws Exception {
                            log.info("[tcp-client] Response-ACK消息接收:" + request);

                            //ackIsOk[0] = checkAckByMsgId(request);
                            //  updateRequestLog(requestId, "success", (int)(System.currentTimeMillis() - start), request);
                            ctx.close();
                        }
                    });
                }
            });

            Channel ch = b.connect(host, port).sync().channel();

            ByteBuf[] delimiter = new ByteBuf[]{Unpooled.wrappedBuffer(new byte[]{0x02}), Unpooled.wrappedBuffer(new byte[]{0x03})};
            //ch.write(delimiter[0]);
            ch.write(inputString);
            //ch.writeAndFlush(delimiter[1]);
            log.info("[tcp-client] 发送请求：{}", inputString);
            ch.close();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            group.shutdownGracefully();
        }
    }

    /**
     * 数据协议转换， 将 inputjson 转换成客户要求的格式
     *
     * @param apiUrl
     * @param inputJson
     * @return
     */
    private static String CHECK_INT50_NO_STOCK = "";


}