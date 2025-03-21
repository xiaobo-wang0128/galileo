package org.armada.galileo.rainbow_gate.transfer.domain.protocol;

/**
 * @author xiaobo
 * @date 2023/2/20 10:21
 */
public class RainbowRequestType {

    /**
     * 请求接口
     */
    public static final Integer Request = 1;

    /**
     * 检查结果
     */
    public static final Integer Check = 2;

    /**
     * 网络检查
     */
    public static final Integer Ping = 3;

    /**
     * 网络双向检测
     */
    public static final Integer Ping_Double = 4;

    /**
     * 分组路由
     */
    public static final Integer Group_Route = 5;

}
