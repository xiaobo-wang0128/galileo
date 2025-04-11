package org.armada.galileo.rainbow_gate.transfer.domain.protocol;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Map;

@Data
@Accessors(chain = true)
@NoArgsConstructor
public class AppRequestDomain {

    public AppRequestDomain(Integer requestType) {
        this.requestType = requestType;
    }

    /**
     * @see org.armada.galileo.rainbow_gate.transfer.domain.protocol.RainbowRequestType
     */
    private Integer requestType;

    /**
     * 请求号
     */
    private String requestNo;

    /**
     * 路由 key，用于网关路由，用于判断该请求发向哪个地址
     */
    private String routeKey;

    /**
     * 分组值，用于异步调用时消息队列中的排序
     */
    private String groupValue;

    /**
     * 接口class name
     */
    private String className;

    /**
     * 目标方法名
     */
    private String methodName;

    /**
     * 入参类型
     */
    private String[] paramTypeNames;

    /**
     * 入参
     */
    private Object[] paramInputs;


    /**
     * 上下文对象
     */
    private Map<String, Object> context;


}
