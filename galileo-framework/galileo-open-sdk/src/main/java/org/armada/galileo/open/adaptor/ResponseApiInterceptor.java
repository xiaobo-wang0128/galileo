package org.armada.galileo.open.adaptor;

/**
 * haiq 调客户接口时的 http 请求拦截器
 *
 * @author xiaobo
 * @date 2021/11/17 6:07 下午
 */
public interface ResponseApiInterceptor {

    /**
     * 接口请求方法.此方法用于访问客户系统，需要在此方法中完成签名、http请求，不需要做返回值正确性的校验
     *
     * @param customerHttpUrl 客户系统的url
     * @param inputJson       输入参数
     * @return 客户系统的返回结果.需要直接返回结果，不能做任何转换
     * @throws Exception
     */
    String requestCustomer(String customerHttpUrl, String inputJson) throws Exception;

    /**
     * 对返回值的结果进行校验，如果返回出错或不合法，需要在此方法中抛 Exception 异常
     *
     * @param customerJsonResponse 客户接口返回值，来源于 execute 方法
     * @throws Exception
     */
    void checkResponse(String customerJsonResponse) throws Exception;

}
