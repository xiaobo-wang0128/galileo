//package mq_test.inbound_test.message;
//
//import org.armada.galileo.common.util.CommonUtil;
//import org.armada.galileo.config.mq.message.InboundMessage;
//import org.armada.galileo.config.mq.message.OutboundMessage;
//import org.armada.galileo.mq.MqInit;
//import org.armada.galileo.mq.MqPublishUtil;
//import org.junit.Test;
//
///**
// * @author xiaobo
// * @date 2023/1/30 10:55
// */
//// @RunWith(SpringRunner.class)
////@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = {StartApplication.class})
//public class InboundMessageTest {
//
//    @Test
//    public void testSend() {
//
//        MqPublishUtil mqPublishUtil = MqInit.initMqPublishUtil();
//
//        String seq = CommonUtil.getRandomNumber(5);
//
//        for (int i = 0; i < 20; i++) {
//            InboundMessage msg = new InboundMessage();
//            // msg.setParam1("key" + i);
//            msg.setCustomerCode("in_" + seq + "_" + i);
//            msg.setCustomerName("in_" + i);
//            msg.setParam7(Math.random());
//            msg.setParam6(123L);
//            mqPublishUtil.sendMessage(msg);
//        }
//
//        for (int i = 0; i < 10; i++) {
//            OutboundMessage msg = new OutboundMessage();
//            // msg.setParam1("key" + i);
//            msg.setParam2("out_" + seq + "_" + i);
//            msg.setParam3("out_" + i);
//            msg.setParam7(Math.random());
//            msg.setParam6(123L);
//            // mqPublishUtil.sendMessage(msg);
//        }
//
//    }
//}
