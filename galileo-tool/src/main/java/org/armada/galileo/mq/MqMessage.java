package org.armada.galileo.mq;

/**
 * @author xiaobo
 * @description: TODO
 * @date 2023/1/14 16:51
 */
public interface MqMessage {

    /**
     * 返回消息类型的中文描述信息
     *
     * @return
     */
    public String getDesc();

    /**
     * 返回消息的业务标识
     * @return
     */
    public String getKey();

}
