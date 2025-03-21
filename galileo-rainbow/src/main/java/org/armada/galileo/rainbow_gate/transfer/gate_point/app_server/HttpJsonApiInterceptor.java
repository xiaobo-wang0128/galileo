package org.armada.galileo.rainbow_gate.transfer.gate_point.app_server;

import org.armada.galileo.rainbow_gate.transfer.open_api.OpenApiContext;

import java.lang.reflect.Method;

/**
 * 业务应用端 - 开放平台调用拦截器
 *
 * @author xiaobo
 * @date 2024/1/17 10:26
 */
public interface HttpJsonApiInterceptor {

    /**
     * 在执行目标方法前执行
     *
     * @param openApiContext 上下文对象
     * @param target         目标对象
     * @param method         待执行方法
     * @param args           入参
     */
    void before(OpenApiContext openApiContext, Object target, Method method, Object[] args);

    /**
     * 在执行目标方法后执行
     *
     * @param openApiContext 上下文对象
     * @param target         目标对象
     * @param method         待执行方法
     * @param args           入参
     * @param result         方法执行结果
     */
    void after(OpenApiContext openApiContext, Object target, Method method, Object[] args, Object result);
}
