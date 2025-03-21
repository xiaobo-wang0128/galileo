package org.armada.galileo.rainbow_gate.transfer.interceptor;

import org.armada.galileo.rainbow_gate.transfer.domain.protocol.AppRequestDomain;
import org.armada.galileo.rainbow_gate.transfer.domain.protocol.AppResponseDomain;

/**
 * rainbow 拦截器
 * @author xiaobo
 * @description: TODO
 * @date 2021/7/14 8:32 下午
 */
public interface RainbowInterceptor {

    /**
     * 在调用前执行
     * @param appRequestDomain
     */
    public void before(AppRequestDomain appRequestDomain);

    /**
     * 在调用完成后执行
     * @param appRequestDomain
     * @param appResponseDomain
     */
    public void complete(AppRequestDomain appRequestDomain, AppResponseDomain appResponseDomain);

//    /**
//     * 发生异常后执行
//     * @param appRequestDomain
//     * @param e
//     */
//    public void exception(AppRequestDomain appRequestDomain, Exception e);

}
