//package org.armada.galileo.mq;
//
//import lombok.extern.slf4j.Slf4j;
//import org.apache.rocketmq.client.apis.ClientConfiguration;
//import org.apache.rocketmq.client.apis.ClientException;
//import org.apache.rocketmq.client.apis.ClientServiceProvider;
//import org.apache.rocketmq.client.apis.consumer.*;
//import org.apache.rocketmq.client.apis.message.MessageView;
//import org.armada.galileo.common.util.CommonUtil;
//import org.armada.galileo.common.util.JsonUtil;
//import org.springframework.context.ApplicationContext;
//
//import java.nio.ByteBuffer;
//import java.nio.charset.StandardCharsets;
//import java.time.Duration;
//import java.util.Collections;
//import java.util.List;
//import java.util.Map;
//import java.util.stream.Collectors;
//
///**
// * @author xiaobo
// * @date 2023/1/14 18:10
// */
//@Slf4j
//public class MqConsumeUtil {
//
//    final ClientServiceProvider provider = ClientServiceProvider.loadService();
//
//    private String endpoints;
//
//    private List<Class<? extends MqMessageListener>> mqMessageListeners;
//
//    private String appName;
//
//    private String hostIp;
//
//    private ApplicationContext applicationContext;
//
//    public MqConsumeUtil(ApplicationContext applicationContext, String appName, String endpoints, List<Class<? extends MqMessageListener>> mqMessageListeners) {
//        this.applicationContext = applicationContext;
//        this.appName = appName;
//        this.hostIp = CommonUtil.getLocalIpAddress().get(0);
//        this.endpoints = endpoints;
//        this.mqMessageListeners = mqMessageListeners;
//    }
//
//    public void doConsume() {
//
//        // key : mstType
//        Map<Class, List<MqMessageListener>> listMap = mqMessageListeners.stream().map(e -> {
//            MqMessageListener mqMessageListener = (MqMessageListener) applicationContext.getAutowireCapableBeanFactory().createBean(e);
//            log.info("[mq-consume] regist mq consumer, class:{}, msgType:{}", e.getName(), mqMessageListener.getMessageType().getName());
//            return mqMessageListener;
//        }).collect(Collectors.groupingBy(e -> {
//            Class msgType = e.getMessageType();
//            return msgType;
//        }));
//
//
//        for (Map.Entry<Class, List<MqMessageListener>> entry : listMap.entrySet()) {
//
//            Class msgType = entry.getKey();
//            String topic = msgType.getName();
//            topic = topic.substring(topic.lastIndexOf(".") + 1);
//
//            List<MqMessageListener> listeners = entry.getValue();
//
//            MqMessageListener.ListenType listenType = listeners.get(0).getListenType();
//
//            // 单点消费
//            if (listenType == MqMessageListener.ListenType.Single) {
//                simpleConsume(topic, appName, msgType, listeners);
//            }
//            //  广播消费
//            else {
//                simpleConsume(topic, appName + "_" + hostIp, msgType, listeners);
//            }
//        }
//
//    }
//
//
//    public void simpleConsume(String topic, String consumerGroup, Class msgType, List<MqMessageListener> listeners) {
//
//        new Thread("mq-consume-" + topic) {
//            @Override
//            public void run() {
//                try {
//                    String tag = "*";
//                    FilterExpression filterExpression = new FilterExpression(tag, FilterExpressionType.TAG);
//                    SimpleConsumer simpleConsumer = provider.newSimpleConsumerBuilder()
//                            //设置消费者分组。
//                            .setConsumerGroup(consumerGroup)
//                            //设置接入点。
//                            .setClientConfiguration(ClientConfiguration.newBuilder().setEndpoints(endpoints).build())
//                            //设置预绑定的订阅关系。
//                            .setSubscriptionExpressions(Collections.singletonMap(topic, filterExpression)).setAwaitDuration(Duration.ofSeconds(1)).build();
//                    List<MessageView> messageViewList = null;
//
//                    while (true) {
//                        try {
//                            //SimpleConsumer需要主动获取消息，并处理。
//                            messageViewList = simpleConsumer.receive(10, Duration.ofSeconds(30));
//                            messageViewList.forEach(messageView -> {
//
//                                //消费处理完成后，需要主动调用ACK提交消费结果。
//                                try {
//
//                                    ByteBuffer byteBuffer = messageView.getBody();
//
//                                    byte[] bufs = new byte[byteBuffer.limit()];
//                                    byteBuffer.get(bufs);
//
//                                    String msgString = new String(bufs, StandardCharsets.UTF_8);
//
//                                    MqMessage obj = (MqMessage) JsonUtil.fromJson(msgString, msgType);
//
//                                    for (MqMessageListener listener : listeners) {
//                                        String listnerClsName = listener.getClass().getName();
//                                        listnerClsName = listnerClsName.substring(listnerClsName.lastIndexOf(".") + 1);
//                                        log.info("[mq-consume] consume mq msg, class:{}, msgId:{}, msgBody:{}", listnerClsName, messageView.getMessageId(), msgString);
//                                        try {
//                                            listener.doConsume(obj);
//                                        } catch (Exception e) {
//                                            log.error("[mq-consume] consume msg error, class:{}, msgId:{}, msgBody:{}", listnerClsName, messageView.getMessageId(), msgString);
//                                            log.error(e.getMessage(), e);
//
//                                            return;
//                                        }
//                                    }
//
//                                    simpleConsumer.ack(messageView);
//
//                                } catch (ClientException e) {
//                                    e.printStackTrace();
//                                }
//                            });
//                        } catch (ClientException e) {
//                            //如果遇到系统流控等原因造成拉取失败，需要重新发起获取消息请求。
//                            log.error(e.getMessage(), e);
//                        }
//                    }
//
//                } catch (Exception e) {
//                    log.error(e.getMessage(), e);
//                }
//            }
//
//        }.start();
//
//    }
//
//
//    // MqMessageListener<? extends MqMessage> listener
//    public void pushConsume(String topic, String consumerGroup, Class msgType, List<MqMessageListener> listeners) {
//
//        ClientConfiguration clientConfiguration = ClientConfiguration.newBuilder().setEndpoints(endpoints).build();
//
//        // push consumer
//
//
//        try {
//
//            //订阅消息的过滤规则，表示订阅所有Tag的消息。
//            String tag = "*";
//            FilterExpression filterExpression = new FilterExpression(tag, FilterExpressionType.TAG);
//            //为消费者指定所属的消费者分组，Group需要提前创建。
//            //初始化PushConsumer，需要绑定消费者分组ConsumerGroup、通信参数以及订阅关系。
//            //PushConsumer pushConsumer =
//
//            // pushconsuer
//            provider.newPushConsumerBuilder().setClientConfiguration(clientConfiguration)
//                    //设置消费者分组。
//                    .setConsumerGroup(consumerGroup)
//                    //设置预绑定的订阅关系。
//                    .setSubscriptionExpressions(Collections.singletonMap(topic, filterExpression)).setMaxCacheMessageCount(20).setConsumptionThreadCount(1)
//
//                    //设置消费监听器。
//                    .setMessageListener(messageView -> {
//
//                        try {
//                            ByteBuffer byteBuffer = messageView.getBody();
//
//                            byte[] bufs = new byte[byteBuffer.limit()];
//                            byteBuffer.get(bufs);
//
//                            //bufs = CommonUtil.uncompress(bufs);
//                            //MqMessage obj = CommonUtil.deserialize(bufs, listener.getMessageType());
//
//                            String msgString = new String(bufs, StandardCharsets.UTF_8);
//                            // log.info("[mq-consumer] receive msg: " + msgString);
//
//                            MqMessage obj = (MqMessage) JsonUtil.fromJson(msgString, msgType);
//
//                            for (MqMessageListener listener : listeners) {
//                                String listnerClsName = listener.getClass().getName();
//                                listnerClsName = listnerClsName.substring(listnerClsName.lastIndexOf(".") + 1);
//                                log.info("[mq-consume] consume mq msg, class:{}, msgId:{}, msgBody:{}", listnerClsName, messageView.getMessageId(), msgString);
//                                try {
//                                    listener.doConsume(obj);
//                                } catch (Exception e) {
//                                    log.error("[mq-consume] consume msg error, class:{}, msgId:{}, msgBody:{}", listnerClsName, messageView.getMessageId(), msgString);
//                                    log.error(e.getMessage(), e);
//
//                                    return ConsumeResult.FAILURE;
//                                }
//                            }
//
//                        } catch (Exception e) {
//                            log.error(e.getMessage(), e);
//
//                            return ConsumeResult.FAILURE;
//                        }
//
//                        return ConsumeResult.SUCCESS;
//                    }).build();
//
//        } catch (Exception e) {
//            log.error(e.getMessage(), e);
//        }
//    }
//}
