package org.armada.galileo.config.mq.message;

import lombok.Data;
import org.armada.galileo.mq.MqMessage;

import java.util.Date;

/**
 * @author xiaobo
 * @date 2023/1/29 14:28
 */
@Data
public class OutboundMessage implements MqMessage {

    @Override
    public String getDesc() {
        return "出库成功";
    }

    @Override
    public String getKey() {
        return param1;
    }

    private String param1;
    private String param2;
    private String param3;
    private Integer param4;
    private Date param5;
    private Long param6;
    private Double param7;

}
