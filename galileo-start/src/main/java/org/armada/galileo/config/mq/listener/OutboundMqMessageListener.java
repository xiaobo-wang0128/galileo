package org.armada.galileo.config.mq.listener;

import lombok.extern.slf4j.Slf4j;
import org.armada.galileo.common.util.CommonUtil;
import org.armada.galileo.common.util.JsonUtil;
import org.armada.galileo.config.mq.message.InboundMessage;
import org.armada.galileo.config.mq.message.OutboundMessage;
import org.armada.galileo.exception.BizException;
import org.armada.galileo.mq.MqMessageListener;

import java.util.Date;

/**
 * @author xiaobo
 * @date 2023/1/29 14:40
 */
@Slf4j
public class OutboundMqMessageListener extends MqMessageListener<OutboundMessage> {

    @Override
    public void comsume(OutboundMessage mqMessage) {
//        log.info(CommonUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss"));
//        log.info("OutboundMqMessageListener: " + JsonUtil.toJson(mqMessage));

        if (Math.random() > 0.5) {
            // throw new BizException("random error");
        }
    }

}
