//package org.armada.galileo.mq;
//
//import lombok.extern.slf4j.Slf4j;
//import org.apache.rocketmq.client.apis.ClientConfiguration;
//import org.apache.rocketmq.client.apis.ClientConfigurationBuilder;
//import org.apache.rocketmq.client.apis.ClientServiceProvider;
//import org.apache.rocketmq.client.apis.message.Message;
//import org.apache.rocketmq.client.apis.producer.Producer;
//import org.armada.galileo.common.util.CommonUtil;
//import org.armada.galileo.common.util.JsonUtil;
//
//import java.nio.charset.StandardCharsets;
//import java.util.HashMap;
//import java.util.Map;
//
///**
// * @author xiaobo
// * @date 2023/1/14 16:52
// */
//@Slf4j
//public class MqPublishUtil {
//
//    private String endpoint;
//
//    private static Map<String, Producer> topicProducer = new HashMap<>();
//
//    public MqPublishUtil(String endpoint) {
//        this.endpoint = endpoint;
//    }
//
//    public void sendMessage(MqMessage mqMessage) {
//        try {
//
//            String topic = mqMessage.getClass().getName();
//            topic = topic.substring(topic.lastIndexOf(".") + 1);
//
////            byte[] bufs = CommonUtil.serialize(mqMessage);
////            bufs = CommonUtil.compress(bufs);
//
//            send(topic, null, mqMessage.getKey(), JsonUtil.toJson(mqMessage));
//
//        } catch (Exception e) {
//            log.error(e.getMessage(), e);
//        }
//    }
//
//    static ClientServiceProvider provider = ClientServiceProvider.loadService();
//
//    private void send(String topic, String tag, String key, String mqMessage) {
//
//        //接入点地址，需要设置成Proxy的地址和端口列表，一般是xxx:8081;xxx:8081。
//        Producer producer = topicProducer.get(topic);
//
//        if (producer == null) {
//            synchronized (log) {
//                producer = topicProducer.get(topic);
//                if (producer == null) {
//                    while (true) {
//
//                        try {
//                            // ClientServiceProvider provider = ClientServiceProvider.loadService();
//                            ClientConfigurationBuilder builder = ClientConfiguration.newBuilder().setEndpoints(endpoint);
//                            ClientConfiguration configuration = builder.build();
//                            //初始化Producer时需要设置通信配置以及预绑定的Topic。
//                            producer = provider.newProducerBuilder()
//                                    .setTopics(topic)
//                                    .setClientConfiguration(configuration)
//                                    .build();
//
//                            topicProducer.put(topic, producer);
//
//                            break;
//                        } catch (Exception e) {
//                            log.error(e.getMessage(), e);
//                            try {
//                                Thread.sleep(5000);
//                            } catch (Exception ex) {
//                            }
//                        }
//                    }
//                }
//            }
//        }
//
//        try {
//            //消息发送的目标Topic名称，需要提前创建。
////            ClientServiceProvider provider = ClientServiceProvider.loadService();
////            ClientConfigurationBuilder builder = ClientConfiguration.newBuilder().setEndpoints(endpoint);
////            ClientConfiguration configuration = builder.build();
////            //初始化Producer时需要设置通信配置以及预绑定的Topic。
////            Producer producer = provider.newProducerBuilder()
////                    .setTopics(topic)
////                    .setClientConfiguration(configuration)
////                    .build();
//
//            byte[] messageBody = mqMessage.getBytes(StandardCharsets.UTF_8);
//
//            //普通消息发送。
//            Message message = provider.newMessageBuilder()
//                    .setTopic(topic)
//                    //设置消息索引键，可根据关键字精确查找某条消息。
//                    .setKeys(key != null ? key : "-")
//                    //设置消息Tag，用于消费端根据指定Tag过滤消息。
//                    // .setTag("messageTag")
//                    //消息体。
//                    .setBody(messageBody)
//                    .build();
//
//            //发送消息，需要关注发送结果，并捕获失败等异常。
//            producer.send(message);
//
//            log.info("[mq-producer] topic:{}, msgBody:{}", topic, mqMessage);
//
//            //SendReceipt sendReceipt = producer.send(message);
//            //sendReceipt.
//        } catch (Exception e) {
//            log.error(e.getMessage(), e);
//        }
//    }
//
//
//}
