package mq_test;

import java.nio.ByteBuffer;

/**
 * @author xiaobo
 * @date 2023/1/14 15:36
 */
public class MqConfigTest {

    public static final String mqEndPoint = "bronze-dev:8081";

    public static void main(String[] args) {

        ByteBuffer byteBuffer = ByteBuffer.allocate(100);


        byteBuffer.put((byte) 112);
        byteBuffer.put((byte) 112);
        byteBuffer.put((byte) 112);
        byteBuffer.put((byte) 112);
        byteBuffer.put((byte) 112);


        byteBuffer.flip();


        System.out.println(byteBuffer);
    }
}
