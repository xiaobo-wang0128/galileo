package org.armada.galileo.rainbow_gate.transfer.open_api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;


/**
 * 开放平台接口调用上下文对象，用于传递第三方平台信息 <br/>
 * 标记为 OpenApiMethod 的开放平台回传接口，需要在入参中指定该参数 <br/>
 * <font color="red">其中 appId 参数由开放平台传入给业务系统，需要各业务系统持久化到业务表中； 在进行回传时需要将该字段原路返回给开放平台</font> <br/>
 * 如果调用时，该字段值为空值，会直接将该请求发送给内部系统 <br/>
 *
 * @author xiaobo
 * @date 2023/4/19 17:40
 */
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class OpenApiContext {

    /**
     * 该平台对应的 货代租户 id
     */
    private Long agentId;

    /**
     * 开放平台应用id
     */
    private String appId;

    /**
     * 开放平台应用第三方平台编码
     */
    private String platformCode;

    /**
     * 开放平台应用第三方平台名称
     */
    private String platformName;

    /**
     * 客户id
     */
    private Long customerId;

    /**
     * 客户编码
     */
    private String customerCode;

    /**
     * 客户名称
     */
    private String customerName;

}
