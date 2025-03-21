/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package transfer_code_test.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;
import org.armada.galileo.common.util.CommonUtil;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Simplistic telnet server.
 */
@Slf4j
public class AdiServer implements Runnable {

    public static void main(String[] args) {
        new Thread(new AdiServer()).start();
    }

    private static final int PORT = 33031;
    private static final ByteBuf[] delimiter = new ByteBuf[]{Unpooled.wrappedBuffer(new byte[]{0x02}), Unpooled.wrappedBuffer(new byte[]{0x03})};

    private static final byte[] splitStart = "<STX>".getBytes(), splitEnd = "<ETX>".getBytes();

    private static final StringDecoder DECODER = new StringDecoder(Charset.forName("utf-8"));
    private static final StringEncoder ENCODER = new StringEncoder(Charset.forName("utf-8"));

    @Override
    public void run() {
        // Configure SSL.
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class).handler(new LoggingHandler(LogLevel.INFO)).childHandler(new ChannelInitializer<SocketChannel>() {

                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    ChannelPipeline pipeline = ch.pipeline();

                    //pipeline.addLast(new AdiServerDecoder((splitStart), (splitEnd)));
                    pipeline.addLast(DECODER);
                    pipeline.addLast(ENCODER);

                    pipeline.addLast(new SimpleChannelInboundHandler<String>() {
                        @Override
                        public void channelRead0(ChannelHandlerContext ctx, String request) throws Exception {

                            System.out.println("[tcp-server] 收到请求, ip: {}, request: {}" + ctx.channel().remoteAddress() + ", " + request);
//                                    try {
//                                        Thread.sleep(5000);
//                                    } catch (Exception e) {
//                                        log.error(e.getMessage(), e);
//                                    }
//
//                                    if (CommonUtil.isEmpty(request)) {
//                                        return;
//                                    }
                            // 在这里处理客户的请求
                            // String resultJson = doTcpRequest(request);
                            // 放入回调队列
//                                    HaiqToCustomerUtil.push(UUID.randomUUID().toString(), null, resultJson);
                            //ctx.channel().write(Unpooled.wrappedBuffer(splitStart));

                            // ctx.channel().write(delimiter[0]);
                            ctx.channel().write(ctx.channel().remoteAddress() + " : " + CommonUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss"));
//                                    ctx.channel().writeAndFlush(Unpooled.wrappedBuffer(splitStart));
                            // ctx.channel().write(delimiter[1]);
                            ctx.channel().flush();
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
                            System.out.println("[tcp-server] 远程客户已连接: " + ctx.channel().remoteAddress());
                        }

                        @Override
                        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
                            super.channelInactive(ctx);
                            System.out.println("[tcp-server] 远程客户已断开: " + ctx.channel().remoteAddress());
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

}
