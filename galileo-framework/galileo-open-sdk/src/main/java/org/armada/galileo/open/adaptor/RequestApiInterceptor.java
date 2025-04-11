package org.armada.galileo.open.adaptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * customer -> haiq http请求拦截器
 *
 * @author xiaobo
 * @description: TODO
 * @date 2021/11/16 8:21 下午
 */
public interface RequestApiInterceptor {

    /**
     * 接口地址路由
     * @param apiCode
     * @param request
     * @return
     */
    public String route(String apiCode, HttpServletRequest request);

    /**
     * 前置拦截校验.一般用于做权限、签名校验，抛异常即可终断执行
     *
     * @param apiCode    接口
     * @param inputJson 输入参数
     * @param request
     */
    public void preCheck(String apiCode, String inputJson, HttpServletRequest request) throws Exception;

    /**
     * 执行成功后的处理.需要在此方法中按照客户的返回值要求，并调用 response.getOutputStream() 方法回写信息
     *
     * @param apiCode     接口
     * @param outputJson 输出信息
     * @param response
     * @return 返回最终输出给客户系统的 json
     */
    public String afterSuccess(String apiCode, String outputJson, HttpServletResponse response) throws Exception;

    /**
     * 发生异常时的处理.需要在此方法中按客户的要求封装错误信息，并调用 response.getOutputStream() 方法回写信息
     *
     * @param errorCode
     * @param errorMsg
     * @param response
     * @return 返回最终输出给客户系统的 json
     */
    public String afterException(String apiCode, Integer errorCode, String errorMsg, HttpServletResponse response) throws Exception;
}
