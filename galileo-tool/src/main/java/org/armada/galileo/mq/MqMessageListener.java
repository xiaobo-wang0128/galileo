package org.armada.galileo.mq;

import org.armada.galileo.common.util.CommonUtil;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import static org.armada.galileo.mq.MqMessageListener.ListenType.Single;

/**
 * @author xiaobo
 * @description: TODO
 * @date 2023/1/14 17:52
 */
public abstract class MqMessageListener<T extends MqMessage> {

    /**
     * 框架封装， 不要重写此方法
     *
     * @return
     */
    public Class<T> getMessageType() {
        return (Class<T>) CommonUtil.getSuperClassGenericType(getClass(), 0);
    }

    /**
     * 框架封装， 不要重写此方法
     *
     * @param obj
     */
    public void doConsume(Object obj) {
        this.comsume((T) obj);
    }

    /**
     * 返回消费类型， 需要进行广播消费时，重写此方法
     *
     * @return
     */
    public ListenType getListenType() {
        return Single;
    }

    /**
     * 消费消息， <font color=red>注意：不要在此方法中启新的线程</font>
     *
     * @param t
     */
    protected  abstract void comsume(T t);

    /**
     * 消息接收类型
     */
    public static enum ListenType {

        /**
         * 单点消费（集群中只能一个实例去消费消息）
         */
        Single,

        /**
         * 广播消费（集群中每个实例都会消费同一条消息，适用于本地缓存刷新）
         */
        Brodcast
    }
}
