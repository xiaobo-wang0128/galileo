package org.armada.galileo.config.mq.listener;

import lombok.extern.slf4j.Slf4j;
import org.armada.galileo.common.util.CommonUtil;
import org.armada.galileo.common.util.JsonUtil;
import org.armada.galileo.config.mq.message.InboundMessage;
import org.armada.galileo.mq.MqMessageListener;

import java.util.Date;

/**
 * @author xiaobo
 * @date 2023/1/29 14:40
 */
@Slf4j
public class InboundMqMessageListener extends MqMessageListener<InboundMessage> {

    @Override
    public void comsume(InboundMessage mqMessage) {
        //  log.info(CommonUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss"));
        log.info(CommonUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss") + " InboundMqMessageListener: " + JsonUtil.toJson(mqMessage));
        try {
            //Thread.sleep(Integer.MAX_VALUE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
