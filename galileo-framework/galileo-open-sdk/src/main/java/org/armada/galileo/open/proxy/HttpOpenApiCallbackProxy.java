package org.armada.galileo.open.proxy;

import lombok.extern.slf4j.Slf4j;
import org.armada.galileo.annotation.openapi.ApiRole;
import org.armada.galileo.common.util.CommonUtil;
import org.armada.galileo.common.util.HttpUtil;
import org.armada.galileo.common.util.JsonUtil;
import org.armada.galileo.common.util.ParameterNameDiscovererCache;
import org.armada.galileo.open.adaptor.ResponseApiInterceptor;
import org.armada.galileo.open.constant.RequestErrorType;
import org.armada.galileo.open.constant.RequestMessageStatus;
import org.armada.galileo.open.dal.entity.OpenInterfaceConfig;
import org.armada.galileo.open.dal.entity.OpenRequestMessage;
import org.armada.galileo.open.domain.SdkGlobalConfig;
import org.armada.galileo.open.proxy.quene.HaiqToCustomerUtil;
import org.armada.galileo.open.service.OpenApiService;
import org.armada.galileo.open.util.ApiDataCache;
import org.armada.galileo.open.util.JsExecutorUtil;
import org.armada.galileo.open.util.RequestMessageQueneUtil;
import org.armada.galileo.open.util.SdkProxyClassCache;
import org.armada.galileo.exception.BizException;
import org.armada.galileo.open.util.api_scan.domain.DocItem;
import org.springframework.util.ReflectionUtils;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * @author xiaobo
 * @date 2021/11/17 6:11 下午
 */
@Slf4j
public class HttpOpenApiCallbackProxy implements InvocationHandler {

    private OpenApiService OpenApiService;

    private static Executor ex = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    public static <T> T getProxy(Class<T> type, OpenApiService OpenApiService) {
        HttpOpenApiCallbackProxy handler = new HttpOpenApiCallbackProxy(OpenApiService);
        return (T) Proxy.newProxyInstance(HttpOpenApiCallbackProxy.class.getClassLoader(), new Class<?>[]{type}, handler);
    }

    public HttpOpenApiCallbackProxy(OpenApiService OpenApiService) {
        this.OpenApiService = OpenApiService;
    }

    /**
     * Object 类的方法
     */
    static List<String> objectMethodNames = new ArrayList<String>();

    static {
        Method[] methods = Object.class.getMethods();
        for (Method m : methods) {
            if (!objectMethodNames.contains(m.getName())) {
                objectMethodNames.add(m.getName());
            }
        }
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (objectMethodNames.contains(method.getName())) {
            return method.invoke(this, args);
        }

        // void 方法默认为 异步调用
        boolean isAsync = method.getReturnType().getName().equals("void") ? true : false;

        if (isAsync) {
            ex.execute(() -> {
                try {
                    invokeMethod(method, args, isAsync);
                } catch (Throwable e) {
                    log.error(e.getMessage(), e);
                }
            });
        } else {
            try {
                return invokeMethod(method, args, isAsync);
            } catch (Throwable e) {
                log.error(e.getMessage(), e);
            }
        }

        return null;

    }

    public Object invokeMethod(Method method, Object[] args, boolean isAsync) throws Throwable {

        ResponseApiInterceptor executor = (ResponseApiInterceptor) SdkProxyClassCache.get(SdkProxyClassCache.ClassType.HaiqToCustomerGlobal.toString());

        Class<?> cls = method.getDeclaringClass();
        String apiUrl = cls.getName() + "." + method.getName();

        Object result = null;
        Exception ex = null;

        Long happenTime = System.currentTimeMillis();
        long l1 = System.currentTimeMillis();

        // 回传给用户的 json
        String inputJson = null;
        // 客户系统返回的 json
        String resultJson = null;
        // 分组字段
        String msgGroup = null;
        // 排序字段
        Integer msgSort = 0;

        DocItem docItem = ApiDataCache.getByApiUrl(apiUrl);

        SdkGlobalConfig.GlobalConfig haiqToCustomerConfig = null;

        try {
            if (args != null && args.length > 0 && method.getAnnotations() != null && method.getAnnotations().length > 0) {

                Annotation annotation = method.getAnnotations()[0];
                Method groupMethod = annotation.getClass().getMethod("group");
                Method sortMethod = annotation.getClass().getMethod("sort");

                if (groupMethod != null && sortMethod != null) {

                    String field = groupMethod.invoke(annotation).toString();
                    msgSort = Integer.valueOf(sortMethod.invoke(annotation).toString());

                    if (CommonUtil.isNotEmpty(field)) {

                        String[] tmps = field.split("\\.");

                        Object target = args[0];
                        if (target instanceof List) {
                            List<Object> tmpList = (List<Object>) target;
                            if (tmpList.size() > 0) {
                                target = tmpList.get(0);
                            }
                        }

                        for (String tmp : tmps) {
                            Field f = ReflectionUtils.findField(target.getClass(), tmp);
                            ReflectionUtils.makeAccessible(f);
                            target = ReflectionUtils.getField(f, target);
                            if (target == null) {
                                break;
                            }

                            if (target instanceof List) {
                                List<Object> tmpList = (List<Object>) target;
                                if (tmpList.size() > 0) {
                                    target = tmpList.get(0);
                                }
                            }
                        }

                        if (target != null) {
                            msgGroup = target.toString();
                        }
                    }

                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }


        // 禁用了回传接口
        if (CommonUtil.isEmpty(haiqToCustomerConfig.getApiInterface()) || !haiqToCustomerConfig.getApiInterface().contains(apiUrl)) {
            log.warn("[nova-sdk] 回传接口被禁用, apiUrl: " + apiUrl);
            return null;
        }

        try {

            // 回传给用户的 json
            String jsonToCustomer = toJson(method, args);

            if ("http".equals(haiqToCustomerConfig.getCallType())) {

                if (executor == null) {
                    log.warn("[nova-sdk] 未配置\"haiq to customer 拦截器层代码\" , 将使用默认 postJson 方法请求客户接口");
                }

                OpenInterfaceConfig apiTransfer = OpenApiService.queryInterfaceByAppUrl(null, apiUrl);

                // 入参 transfer
                if (apiTransfer != null && CommonUtil.isNotEmpty(apiTransfer.getInput())) {
                    jsonToCustomer = JsExecutorUtil.executeJs(jsonToCustomer, apiTransfer.getInput());
                }
                inputJson = new String(jsonToCustomer);

                // 请求客户服务器
                String customerJsonResult = null;

                // 配置了拦截器，调拦截器代码
                if (executor != null) {
                    customerJsonResult = executor.requestCustomer(apiTransfer != null ? apiTransfer.getHttpUrl() : null, jsonToCustomer);
                }
                // 未配置则直接根据适配层的配置进行回调
                else {
                    // 未配置
                    if (apiTransfer == null || CommonUtil.isEmpty(apiTransfer.getHttpUrl())) {
                        log.warn("[nova-sdk] " + (docItem != null ? docItem.getApiName() + " " : " ") + apiUrl + " 接口回传未配置，本次调用将不会完成回传");
                        customerJsonResult = "接口适配未配置, mock 回调成功，未调用真实客户接口";
                    }
                    // 回调客户地址
                    else {
                        customerJsonResult = HttpUtil.postJson(apiTransfer.getHttpUrl(), jsonToCustomer);
                    }
                }

                // 记录客户的原始返回值
                resultJson = new String(customerJsonResult);

                // 调用适配类校验客户返回的结果，在此方法中对客户的返回值进行统一的校验
                if (executor != null) {
                    executor.checkResponse(customerJsonResult);
                }

                // void 异步请求
                if (isAsync) {
                    result = resultJson;
                }
                // 非 void 采用同步请求 ， 做一次结果转换，转换成接口定义返回值所需要的类型
                else {
                    if (apiTransfer != null && CommonUtil.isNotEmpty(apiTransfer.getOutput())) {
                        customerJsonResult = JsExecutorUtil.executeJs(customerJsonResult, apiTransfer.getOutput());
                    }
                    result = JsonUtil.fromJson(customerJsonResult, method.getGenericReturnType());
                }

            } else {

                inputJson = jsonToCustomer;

            }

        } catch (Exception e) {
            ex = e;
            log.error(e.getMessage(), e);

        } finally {

            OpenRequestMessage rm = new OpenRequestMessage();

            rm.setIsAsync(isAsync ? "Y" : "N");
            // 请求号
            rm.setRequestId(UUID.randomUUID().toString());
            // 调用接口名
            rm.setApiUrl(apiUrl);
            // 调用方法名
            if (docItem != null) {
                rm.setApiName(docItem.getApiName());
            } else {
                rm.setApiName("未知接口");
            }
            // 发生时间
            rm.setHappenTime(happenTime);
            // 传输方向
            // rm.setApiType(OpenApiType.ResponseApi);
            rm.setApiFrom(ApiRole.LOCAL);
            // 状态
            if (ex != null) {
                rm.setStatus(RequestMessageStatus.FAIL);
                // 错误类型
                if (ex instanceof IOException) {
                    rm.setErrorType(RequestErrorType.IO);
                } else {
                    rm.setErrorType(RequestErrorType.BIZ);
                }
            } else {
                rm.setStatus(RequestMessageStatus.SUCCESS);
            }

            rm.setRequestSize(inputJson != null ? inputJson.getBytes(StandardCharsets.UTF_8).length : 0);
            rm.setResponseSize(resultJson != null ? resultJson.getBytes(StandardCharsets.UTF_8).length : 0);
            rm.setRequestJson(inputJson);
            rm.setResponseJson(resultJson);
            rm.setTimeCost((int) (System.currentTimeMillis() - l1));
            rm.setErrorMessage(ex != null ? ex.getMessage() : "");
            rm.setUpdateTime(System.currentTimeMillis());
            rm.setRetryTime(0);
            rm.setMsgGroup(msgGroup);
            rm.setMsgSort(msgSort);

            // 基础通信方式， 将回传信息写至缓冲队列
            if (haiqToCustomerConfig != null && asyncNofityTypes.contains(haiqToCustomerConfig.getCallType())) {
                rm.setStatus(RequestMessageStatus.DOING);
                OpenApiService.insert(rm);

                // 涉及消息分组提序， 所以不能在产生消息后立刻回传
                // HaiqToCustomerUtil.push(rm.getRequestId(), rm.getUpdateTime(), apiUrl, inputJson);

            } else {
                RequestMessageQueneUtil.push(rm);
            }
        }

        if (ex != null) {
            throw new BizException(ex);
        }

        return result;
    }

    private static List<String> asyncNofityTypes = CommonUtil.asList("socket", "ftp", "db");

    /**
     * 回传重试
     *
     * @param msgList
     */
    public void retry(List<OpenRequestMessage> msgList, boolean groupMsg, boolean auto) {

        if (CommonUtil.isEmpty(msgList)) {
            return;
        }

        SdkGlobalConfig.GlobalConfig haiqToCustomer = null;

        if (haiqToCustomer != null && asyncNofityTypes.contains(haiqToCustomer.getCallType())) {
            HaiqToCustomerUtil.push(msgList, groupMsg, auto);

            return;
        }

        String apiUrl = msgList.get(0).getApiUrl();

        ResponseApiInterceptor adaptor = (ResponseApiInterceptor) SdkProxyClassCache.get(SdkProxyClassCache.ClassType.HaiqToCustomerGlobal.toString());

        OpenInterfaceConfig apiTransfer = OpenApiService.queryInterfaceByAppUrl(null, apiUrl);

        for (OpenRequestMessage msg : msgList) {

            String requestId = msg.getRequestId();
            String requestJson = msg.getRequestJson();

            if (CommonUtil.isEmpty(requestJson)) {

                msg.setUpdateTime(System.currentTimeMillis());
                msg.setRetryTime(msg.getRetryTime() != null ? msg.getRetryTime() + 1 : 1);
                msg.setStatus(RequestMessageStatus.FAIL);
                msg.setErrorMessage("回传内容为空");
                OpenApiService.update(msg);

                return;
                //throw new BizException("回传内容不能为空, 且必须是 json 格式");
            }


            // 请求客户服务器
            String responseJson = null;

            RequestMessageStatus status = null;

            RequestErrorType errType = null;

            Exception ex = null;

            try {

                // 请求客户服务器
                responseJson = adaptor.requestCustomer(apiTransfer.getHttpUrl(), requestJson);

                // 调用适配类校验客户返回的结果，在此方法中对客户的返回值进行统一的校验
                adaptor.checkResponse(responseJson);

            } catch (Exception e) {
                ex = e;
                log.error(e.getMessage(), e);
            }

            // 状态
            if (status == null) {
                if (ex != null) {
                    status = RequestMessageStatus.FAIL;
                    // 错误类型
                    if (ex instanceof IOException) {
                        errType = RequestErrorType.IO;
                    } else {
                        errType = RequestErrorType.BIZ;
                    }
                } else {
                    status = RequestMessageStatus.FAIL;
                }
            }

            // 状态

            msg.setUpdateTime(System.currentTimeMillis());
            msg.setRetryTime(msg.getRetryTime() != null ? msg.getRetryTime() + 1 : 1);
            msg.setStatus(status);
            msg.setErrorType(errType);
            msg.setResponseJson(responseJson);

            OpenApiService.update(msg);
        }


    }

//    public static interface RetryCallback {
//        public void callback(String requestId, String status, String errType, String responseJson);
//    }

    private String toJson(Method method, Object[] args) {

        // 多个参数 需要从map中 找到对应值
        if (args == null || args.length == 0) {
            return null;
        }
        if (args.length == 1) {
            return JsonUtil.toJson(args[0]);
        }
        // 按参数名组装 json
        else {
            String[] paramNames = ParameterNameDiscovererCache.getParameterNameDiscoverer().getParameterNames(method);

            Map<String, Object> map = new HashMap<>();

            for (int i = 0; i < paramNames.length; i++) {
                if (args[i] != null) {
                    map.put(paramNames[i], args[i]);
                }
            }
            return JsonUtil.toJson(map);
        }
    }
}
