package org.armada.galileo.open.proxy;

import lombok.extern.slf4j.Slf4j;
import org.armada.galileo.annotation.openapi.ApiRole;
import org.armada.galileo.annotation.openapi.OpenApiMethod;
import org.armada.galileo.common.util.CommonUtil;
import org.armada.galileo.common.util.HttpUtil;
import org.armada.galileo.common.util.JsonUtil;
import org.armada.galileo.exception.BizException;
import org.armada.galileo.open.constant.RequestErrorType;
import org.armada.galileo.open.constant.RequestMessageStatus;
import org.armada.galileo.open.dal.entity.OpenRequestMessage;
import org.armada.galileo.open.domain.SdkGlobalConfig;
import org.armada.galileo.open.service.OpenApiService;
import org.armada.galileo.open.util.RequestMessageQueneUtil;
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
 * @date 2023/2/1 17:36
 */
@Slf4j
public class HttpOpenApiClient implements InvocationHandler {

    private OpenApiService openApiService;

    private static Executor ex = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    private ApiUrlRouter apiUrlRouter;

    private String appId;

    private String appSecret;

    public static <T> T getProxy(Class<T> type, OpenApiService OpenApiService, ApiUrlRouter apiUrlRouter) {
        HttpOpenApiClient handler = new HttpOpenApiClient(OpenApiService);
        handler.apiUrlRouter = apiUrlRouter;
        return (T) Proxy.newProxyInstance(HttpOpenApiCallbackProxy.class.getClassLoader(), new Class<?>[]{type}, handler);
    }

    public HttpOpenApiClient(OpenApiService OpenApiService) {
        this.openApiService = OpenApiService;
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

        Class<?> cls = method.getDeclaringClass();
        String apiUrl = cls.getName() + "." + method.getName();

        Object result = null;
        Exception ex = null;

        long happenTime = System.currentTimeMillis();
        long l1 = happenTime;

        // 回传给用户的 json
        String inputJson = null;
        // 客户系统返回的 json
        String resultJson = null;
        // 分组字段
        String msgGroup = null;
        // 排序字段
        Integer msgSort = 0;

        String apiName = null; // method.getAnnotation(OpenApiMethod.class).name();

        // DocItem docItem = ApiDataCache.getByApiUrl(apiUrl);

        SdkGlobalConfig.GlobalConfig haiqToCustomerConfig = null;

        // 获取入参的回传排序分组字段
        try {
            if (args != null && args.length > 0 && method.getAnnotations() != null && method.getAnnotations().length > 0) {

                Annotation annotation = method.getAnnotations()[0];
                Method groupMethod = annotation.getClass().getMethod("group");
                Method sortMethod = null;

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


        // 回传给用户的 json
        String requestJson = toJson(method, args);

        String apiMethodCode = method.getAnnotation(OpenApiMethod.class).code();

        String requestUrl = apiUrlRouter.getUrl() + "/" + apiMethodCode;

        String appId = apiUrlRouter.getAppId();

        String appSecret = apiUrlRouter.getAppSecret();

        String timestamp = String.valueOf(System.currentTimeMillis());

        String sign = CommonUtil.md5(appSecret + timestamp + requestJson);

        Map<String, String> headers = new HashMap<>();
        headers.put(HttpOpenApServlet.appIdHeader, appId);
        headers.put(HttpOpenApServlet.appSignHeader, sign);
        headers.put(HttpOpenApServlet.appRequestTimeHeader, timestamp);

        try {

            inputJson = new String(requestJson);

            // 请求客户服务器
            String customerJsonResult = null;

            customerJsonResult = HttpUtil.postJson(requestUrl, requestJson, headers, "utf-8", 60000);

            // 记录客户的原始返回值
            resultJson = new String(customerJsonResult);

            // void 异步请求
            if (isAsync) {
                result = resultJson;
            }
            // 非 void 采用同步请求 ， 做一次结果转换，转换成接口定义返回值所需要的类型
            else {
                try {
                    result = JsonUtil.fromJson(customerJsonResult, method.getGenericReturnType());
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
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
            rm.setApiName(apiName);
            // 发生时间
            rm.setHappenTime(happenTime);
            // 传输方向
            //rm.setApiType(OpenApiType.RequestApi);
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
                openApiService.insert(rm);
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

            for (int i = 0; i < args.length; i++) {

//                if (args[i] instanceof OpenApiContext) {
//                    continue;
//                }
//
//                if (method.getParameterTypes()[i].isAnnotationPresent(AppId.class)) {
//                    continue;
//                }
//
//                if (args[i] != null) {
//                    return JsonUtil.toJson(args[i]);
//                }
            }


//            String[] paramNames = ParameterNameDiscovererCache.getParameterNameDiscoverer().getParameterNames(method);
//
//            Map<String, Object> map = new HashMap<>();
//
//            for (int i = 0; i < paramNames.length; i++) {
//
//                if (args[i] instanceof OpenApiContext) {
//                    continue;
//                }
//
//                if (method.getParameterTypes()[i].isAnnotationPresent(AppId.class)) {
//                    continue;
//                }
//
//                if (args[i] != null) {
//                    map.put(paramNames[i], args[i]);
//                }
//            }
//            if (map.size() == 1) {
//                for (Map.Entry<String, Object> entry : map.entrySet()) {
//                    return JsonUtil.toJson(entry.getValue());
//                }
//            }
//
//            return JsonUtil.toJson(map);

            return null;
        }
    }


}
