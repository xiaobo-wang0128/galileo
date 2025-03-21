//package org.armada.galileo.mq;
//
//
//import org.armada.galileo.common.util.CommonUtil;
//import org.armada.galileo.common.util.SpringConfig;
//import org.springframework.beans.BeansException;
//import org.springframework.context.ApplicationContext;
//
//import java.util.List;
//
///**
// * @author xiaobo
// * @date 2023/1/30 14:13
// */
//public class MqInit {
//
//    public static MqPublishUtil initMqPublishUtil() {
//        String endpoint = SpringConfig.getConfig("rocket.endpoint", "bronze-dev:8081");
//        return new MqPublishUtil(endpoint);
//    }
//
//    public static void initConsumer(String appName, List<Class<? extends MqMessageListener>> mqMessageListeners, ApplicationContext applicationContext) {
//
//        String endpoint = SpringConfig.getConfig("rocket.endpoint", "bronze-dev:8081");
//
//        new Thread("mq-consumer") {
//            public void run() {
//                MqConsumeUtil mqConsumeUtil = new MqConsumeUtil(applicationContext, appName, endpoint, mqMessageListeners);
//                mqConsumeUtil.doConsume();
//            }
//        }.start();
//    }
//
//
//}
