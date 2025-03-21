//package mq_test;
//
///**
// * @author xiaobo
// * @date 2023/1/14 15:36
// */
//
//import org.apache.rocketmq.client.apis.ClientConfiguration;
//import org.apache.rocketmq.client.apis.ClientException;
//import org.apache.rocketmq.client.apis.ClientServiceProvider;
//import org.apache.rocketmq.client.apis.consumer.ConsumeResult;
//import org.apache.rocketmq.client.apis.consumer.FilterExpression;
//import org.apache.rocketmq.client.apis.consumer.FilterExpressionType;
//import org.apache.rocketmq.client.apis.consumer.PushConsumer;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.io.IOException;
//import java.nio.ByteBuffer;
//import java.util.Collections;
//
//public class PushConsumerExample {
//    private static final Logger LOGGER = LoggerFactory.getLogger(PushConsumerExample.class);
//
//    private PushConsumerExample() {
//    }
//
//    public static void main(String[] args) throws ClientException, IOException, InterruptedException {
//
//        final ClientServiceProvider provider = ClientServiceProvider.loadService();
//
//        //接入点地址，需要设置成Proxy的地址和端口列表，一般是xxx:8081;xxx:8081。
//        String endpoints = MqConfigTest.mqEndPoint;
//
//        ClientConfiguration clientConfiguration = ClientConfiguration.newBuilder()
//                .setEndpoints(endpoints)
//                .build();
//        //订阅消息的过滤规则，表示订阅所有Tag的消息。
//        String tag = "*";
//        FilterExpression filterExpression = new FilterExpression(tag, FilterExpressionType.TAG);
//        //为消费者指定所属的消费者分组，Group需要提前创建。
//        String consumerGroup = "Your_ConsumerGroup";
//        //指定需要订阅哪个目标Topic，Topic需要提前创建。
//        String topic = "TestTopic";
//        //初始化PushConsumer，需要绑定消费者分组ConsumerGroup、通信参数以及订阅关系。
//        PushConsumer pushConsumer = provider.newPushConsumerBuilder()
//                .setClientConfiguration(clientConfiguration)
//                //设置消费者分组。
//                .setConsumerGroup(consumerGroup)
//                //设置预绑定的订阅关系。
//                .setSubscriptionExpressions(Collections.singletonMap(topic, filterExpression))
//                //设置消费监听器。
//                .setMessageListener(messageView -> {
//                    //处理消息并返回消费结果。
//                    // LOGGER.info("Consume message={}", messageView);
//                    System.out.println("-------------------");
//                     System.out.println(messageView);
//
//                    ByteBuffer byteBuffer = messageView.getBody();
//
//                    //byteBuffer.flip();
//                    byte[] bufs = new byte[byteBuffer.limit() - byteBuffer.position()];
//                    bufs = new byte[byteBuffer.limit()];
//                    byteBuffer.get(bufs);
//                    System.out.println(new String(bufs));
//
//
//                    //byteBuffer.
//
////                    byteBuffer.position();
////
////                    byteBuffer.
//
//                    // ByteBuffer bbb = ByteBuffer.
//
////                    Charset charset =Charset.defaultCharset();
////
////                    CharBuffer str = charset.decode(byteBuffer);
//
//
////                    System.out.println(str.toString());;
////                    c("utf-8")
////                    byteBuffer.
////                    byte[] bufs = byteBuffer.asReadOnlyBuffer().array();
////                    //byte[] bufs = messageView.getBody().array();
//
//                    // messageView.getBody().
//
//                    // messageView.getBody()
////                    System.out.println(new String(bufs));
//                    // System.out.println(new String(messageView.getBody().array()));
//
//                    return ConsumeResult.SUCCESS;
//                })
//                .build();
//        Thread.sleep(Long.MAX_VALUE);
//        //如果不需要再使用PushConsumer，可关闭该进程。
//        //pushConsumer.close();
//    }
//}