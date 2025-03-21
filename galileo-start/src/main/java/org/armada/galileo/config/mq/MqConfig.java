//package org.armada.galileo.config.mq;
//
//import org.armada.galileo.common.util.CommonUtil;
//import org.armada.galileo.config.mq.listener.InboundMqMessageListener;
//import org.armada.galileo.config.mq.listener.OutboundMqMessageListener;
//import org.armada.galileo.mq.MqInit;
//import org.armada.galileo.mq.MqMessageListener;
//import org.armada.galileo.mq.MqPublishUtil;
//import org.springframework.beans.BeansException;
//import org.springframework.context.ApplicationContext;
//import org.springframework.context.ApplicationContextAware;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//import java.util.List;
//
///**
// * @author xiaobo
// * @date 2023/1/29 14:31
// */
//@Configuration
//public class MqConfig implements ApplicationContextAware {
//
//    private static List<Class<? extends MqMessageListener>> mqMessageListeners = CommonUtil.asList(
//            InboundMqMessageListener.class,
//            OutboundMqMessageListener.class
//
//    );
//
//    @Bean
//    public MqPublishUtil initMqPublishUtil() {
//        return MqInit.initMqPublishUtil();
//    }
//
//    @Override
//    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
//        MqInit.initConsumer("test-app", mqMessageListeners, applicationContext);
//    }
//
//
//}
