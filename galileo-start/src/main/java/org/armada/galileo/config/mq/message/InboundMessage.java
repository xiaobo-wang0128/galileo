package org.armada.galileo.config.mq.message;

import lombok.Data;
import org.armada.galileo.mq.MqMessage;

import java.util.Date;

/**
 * @author xiaobo
 * @date 2023/1/29 14:28
 */
@Data
public class InboundMessage implements MqMessage {

    @Override
    public String getDesc() {
        return "入库成功";
    }

    @Override
    public String getKey() {
        return orderNo;
    }

    private String orderNo;
    private String customerCode;
    private String customerName;
    private Integer qty;
    private Date day;
    private Long param6;
    private Double param7;

}
