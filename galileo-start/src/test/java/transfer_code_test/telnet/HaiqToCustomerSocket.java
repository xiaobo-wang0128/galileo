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
package transfer_code_test.telnet;

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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

/**
 * Simplistic telnet client.
 */
public final class HaiqToCustomerSocket {


    private static final int Max_Package_size = 50 * 1024 * 1024;
    private static final ByteBuf[] delimiter = new ByteBuf[]{Unpooled.wrappedBuffer(new byte[]{0x02}), Unpooled.wrappedBuffer(new byte[]{0x03})};
    private static final StringDecoder DECODER = new StringDecoder(Charset.forName("utf-8"));
    private static final StringEncoder ENCODER = new StringEncoder(Charset.forName("utf-8"));

    public static void main(String[] args) throws Exception {

        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group).channel(NioSocketChannel.class).handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    System.out.println(ch.remoteAddress() + "已连接");
                    ChannelPipeline pipeline = ch.pipeline();
                    pipeline.addLast(new DelimiterBasedFrameDecoder(Max_Package_size, delimiter));
                    pipeline.addLast(DECODER);
                    pipeline.addLast(ENCODER);
                }
            });

            Channel ch = b.connect("172.18.81.86", 7203).sync().channel();

            ChannelFuture lastWriteFuture = null;
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            for (; ; ) {
                String line = in.readLine();
                if (line == null) {
                    break;
                }

                if (!ch.isActive()) {
                    return;
                }

                ByteBuf[] delimiter = new ByteBuf[]{Unpooled.wrappedBuffer(new byte[]{0x02}), Unpooled.wrappedBuffer(new byte[]{0x03})};

                ch.write(delimiter[0]);
                ch.write(line);
                ch.writeAndFlush(delimiter[1]);

                if ("bye".equals(line.toLowerCase())) {
                    ch.closeFuture().sync();
                    break;
                }
            }

            if (lastWriteFuture != null) {
                lastWriteFuture.sync();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return;
        } finally {
            group.shutdownGracefully();
        }
    }
}
