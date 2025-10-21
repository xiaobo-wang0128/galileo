package org.armada.galileo.open.proxy;

import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.armada.galileo.annotation.openapi.ApiRole;
import org.armada.galileo.common.util.*;
import org.armada.galileo.open.adaptor.RequestApiInterceptor;
import org.armada.galileo.annotation.openapi.OpenApiMethod;
import org.armada.galileo.open.cache.OpenApiCacheUtil;
import org.armada.galileo.open.constant.RequestErrorType;
import org.armada.galileo.open.constant.RequestMessageStatus;
import org.armada.galileo.open.dal.entity.OpenInterfaceConfig;
import org.armada.galileo.open.dal.entity.OpenRequestMessage;
import org.armada.galileo.open.domain.*;
import org.armada.galileo.open.service.OpenApiService;
import org.armada.galileo.open.util.*;
import org.armada.galileo.exception.BizException;
import org.armada.galileo.mvc_plus.domain.JsonResult;
import org.armada.galileo.open.util.api_scan.domain.DocItem;
import org.springframework.context.ApplicationContext;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * customer to haiq http请求入口
 *
 * @author xiaobo
 * @date 2021/11/5 11:11 下午
 */
@Slf4j
public class HttpOpenApServlet extends HttpServlet {

    private ApplicationContext applicationContext;

    public String uriHead;

    private static final String CONTENT_TYPE_JSON = "application/json; charset=UTF-8";

    // http headers
    public static final String appIdHeader = "x-app-id";
    public static final String appSignHeader = "x-app-sign";
    public static final String appRequestTimeHeader = "x-request-time";

    private static Executor executor = Executors.newFixedThreadPool(20);

    public HttpOpenApServlet(ApplicationContext applicationContext, String uriHead, String appName) {
        if (CommonUtil.isEmpty(uriHead)) {
            throw new RuntimeException("前缀地址不能为空");
        }
        this.applicationContext = applicationContext;
        this.uriHead = uriHead;
    }

    private static OpenApiService openApiService;

    public static void setOpenApiService(OpenApiService openApiService) {
        HttpOpenApServlet.openApiService = openApiService;
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doExecute(false, request, response);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        throw new RuntimeException("not support");
    }

    public void doExecute(boolean debug, HttpServletRequest request, HttpServletResponse response) {

        // 全局配置
        // SdkGlobalConfig sdkGlobalConfig = OpenApiService.getGlobalConfig();

        TimerCheck.start();

        // 拦截器
        RequestApiInterceptor interceptor = null;

        String requestId = UUID.randomUUID().toString();
        // 前置基础校验
        String apiCode = null;

        Exception ex = null;
        int requestByteSize = 0;
        int responseByteSize = 0;
        long happenTime = System.currentTimeMillis();
        long l1 = System.currentTimeMillis();

        // 回传给用户的 json
        String inputJson = null;
        String resultJson = null;

        String apiUrl = request.getRequestURI();

        // 是否异步请求
        boolean async = false;
        try {

            String appId = request.getHeader(appIdHeader);
            String appSign = request.getHeader(appSignHeader);
            String requestTime = request.getHeader(appRequestTimeHeader);

            if (CommonUtil.isEmpty(appId)) {
                throw new RuntimeException("http header '" + appIdHeader + "' is null");
            }
            if (CommonUtil.isEmpty(appSign)) {
                throw new RuntimeException("http header '" + appSignHeader + "' is null");
            }
            if (CommonUtil.isEmpty(requestTime)) {
                throw new RuntimeException("http header '" + appRequestTimeHeader + "' is null");
            }

            long timeStamp = Convert.asLong(requestTime);
            long now = System.currentTimeMillis();
            if (Math.abs(now - timeStamp) > 5 * 60000) {
                throw new RuntimeException("timestamp error, please check your server time");
            }

            TimerCheck.checkpoint("get_param");

            // 签名校验
            byte[] inputBytes = CommonUtil.readJsonFormBytes(request);

            // 传递给系统的报文
            String jsonContent = new String(inputBytes, StandardCharsets.UTF_8);


            TimerCheck.checkpoint("read_params");

            OpenApiCacheUtil.OpenAppCache appCache = OpenApiCacheUtil.getOpenAppCache(appId);
            if (appCache == null) {
                throw new RuntimeException("appId not exist, please check you account config");
            }
            String needCheck = new StringBuffer(appCache.getAppSecret()).append(requestTime).append(jsonContent).toString();
            String md5Check = CommonUtil.md5(needCheck);

            if (!md5Check.equals(appSign)) {
                throw new RuntimeException("api sign error");
            }

            TimerCheck.checkpoint("md5 check");


            requestByteSize = inputBytes.length;
            // 原始报文
            inputJson = new String(jsonContent);


            if (interceptor != null) {
                apiCode = interceptor.route(apiCode, request);
            }

            if (apiCode == null) {

                if (!apiUrl.startsWith(uriHead)) {
                    throw new BizException("接口请求路径不正确: " + apiUrl);
                }

                String end = apiUrl.substring(uriHead.length());
                if (end.startsWith("/")) {
                    end = end.substring(1);
                }

                apiCode = end;
            }

            // transfer 转换器
            OpenInterfaceConfig apiTransfer = OpenApiCacheUtil.getInterfaceConfig(null, apiCode);

            // 拦截器前置处理
            if (interceptor != null) {
                interceptor.preCheck(apiCode, jsonContent, request);
            }

            // transfer 前置处理
            if (apiTransfer != null && CommonUtil.isNotNull(apiTransfer.getInput())) {
                jsonContent = JsExecutorUtil.executeJs(jsonContent, apiTransfer.getInput());
            }

            // 执行目标方法
            Pair<Class, Method> pair = OpenApiCacheUtil.getInvokeMethod(apiCode);
            if (pair == null) {
                throw new BizException("接口地址不存在");
            }
            async = pair.getRight().getAnnotation(OpenApiMethod.class).async();

            JsonResult jr = null;

            TimerCheck.checkpoint("execute method prepare");

            // 异步执行，将待行内容放入队列排队执行
            if (async) {
                jr = new JsonResult();
                AsyncExecutorUtil.push(new AsyncExecutorUtil.RequestTask(requestId, apiCode, jsonContent));
            }
            // 同步执行，直接去调对应的接口方法
            else {
                Object obj = executeRequest(pair, apiCode, jsonContent);
                jr = new JsonResult(obj);
            }

            TimerCheck.checkpoint("execute method");

            String output = JsonUtil.toJson(jr);

            // transfer 后置处理
            if (apiTransfer != null && CommonUtil.isNotEmpty(apiTransfer.getOutput())) {
                output = JsExecutorUtil.executeJs(output, apiTransfer.getOutput());
            }

            byte[] outputBytes = output.getBytes(StandardCharsets.UTF_8);
            responseByteSize = outputBytes.length;

            // 拦截器后置处理
            if (interceptor != null) {
                String tmpOut = interceptor.afterSuccess(apiCode, output, response);
                if (tmpOut != null) {
                    resultJson = new String(tmpOut);
                }
            } else {
                try {
                    response.setContentType(CONTENT_TYPE_JSON);
                    response.getOutputStream().write(outputBytes);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }

            if (resultJson == null) {
                resultJson = new String(output);
            }


            TimerCheck.checkpoint("get result");

        } catch (Exception e) {

            log.error(e.getMessage(), e);

            if (e instanceof InvocationTargetException) {
                e = (Exception) ((InvocationTargetException) e).getTargetException();
            }
            // 获取到最底层的异常
            while (true) {
                if (e.getCause() != null && e.getCause() instanceof Exception) {
                    e = (Exception) e.getCause();
                    continue;
                }
                break;
            }

            Integer errorCode = 500;
            String errorMsg = e.getMessage();

            if (errorMsg == null) {
                errorMsg = "haiq 接口异常";
            }

            // 异常处理
            if (interceptor != null) {
                try {
                    resultJson = interceptor.afterException(apiCode, errorCode, errorMsg, response);
                } catch (Exception exx) {
                    log.error(e.getMessage(), exx);
                }
            } else {
                JsonResult jr = new JsonResult();
                jr.setCode(errorCode);
                jr.setMessage(errorMsg);
                try {
                    response.setContentType(CONTENT_TYPE_JSON);
                    response.getOutputStream().write(JsonUtil.toJson(jr).getBytes(StandardCharsets.UTF_8));
                } catch (Exception ex2) {
                    log.error(ex.getMessage(), ex2);
                }
            }

        } finally {

            TimerCheck.checkpoint("save result");

            DocItem docItme = ApiDataCache.getByApiUrl(apiCode);

            OpenRequestMessage rm = new OpenRequestMessage();

            // 同步、异步
            rm.setIsAsync(async ? "Y" : "N");
            // 状态
            if (async) {
                rm.setStatus(RequestMessageStatus.DOING);
            } else {
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
            }
            // 请求号
            rm.setRequestId(requestId);
            // 调用接口名
            rm.setApiUrl(apiUrl);
            // 调用方法名
            if (docItme != null) {
                rm.setApiName(docItme.getApiName());
            } else {
                rm.setApiName("未知接口");
            }
            // 发生时间
            rm.setHappenTime(happenTime);
            // 传输方向
            rm.setApiTo(ApiRole.LOCAL);

            rm.setRequestSize(requestByteSize);
            rm.setResponseSize(responseByteSize);
            rm.setRequestJson(inputJson);
            rm.setResponseJson(resultJson);
            rm.setTimeCost((int) (System.currentTimeMillis() - l1));
            rm.setErrorMessage(ex != null ? ex.getMessage() : "");
            rm.setUpdateTime(System.currentTimeMillis());
            rm.setRetryTime(0);

            RequestMessageQueneUtil.push(rm);
//            executor.execute(() -> {
//                openApiService.saveUpdateOpenRequestMessage(rm);
//            });

        }
    }

    public String preCheck(SdkGlobalConfig sdkGlobalConfig, HttpServletRequest request, boolean debug) {

        String apiMethod = request.getHeader("X-Haiq-Api-Method");
        String appId = request.getHeader("X-Haiq-App-Id");
        String apiSign = request.getHeader("X-Haiq-Api-Sign");

        if (uriHead != null && !debug) {
            String uri = request.getRequestURI();
            if (!uri.startsWith(uriHead)) {
                throw new BizException("接口请求路径不正确: " + uri);
            }

            String end = uri.substring(uriHead.length());
            if (end.startsWith("/")) {
                end = end.substring(1);
            }
            apiMethod = end;
        }

        if (sdkGlobalConfig != null && sdkGlobalConfig.getCustomerToHaiq() != null) {
            SdkGlobalConfig.GlobalConfig globalConfig = sdkGlobalConfig.getCustomerToHaiq();
            if (globalConfig.getApiInterface() != null && !globalConfig.getApiInterface().contains(apiMethod)) {
                throw new BizException("接口未开启，请在\"接口对接配置\"中配置");
            }
        }

        return apiMethod;
    }

    public Object executeRequest(Pair<Class, Method> pair, String apiUri, String jsonContent) throws Exception {

        if (pair == null) {
            throw new BizException("接口地址不存在");
        }

        Class cls = pair.getLeft();
        Method method = pair.getRight();

        // transfer 层处理
        Object target = null;
        try {
            target = applicationContext.getBean(cls);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new BizException("接口不存在: " + cls.getName());
        }

        if (target == null) {
            throw new BizException("接口不存在: " + cls.getName());
        }

        if (method == null) {
            throw new BizException("没有找到待执行方法: " + apiUri);
        }

        Object[] args = null;

        Type[] inputTypes = method.getGenericParameterTypes();

        TimerCheck.checkpoint("before param change");
        if (inputTypes != null && inputTypes.length > 0) {

            // 多个参数 需要从map中 找到对应值
            if (inputTypes.length > 1) {
                String[] paramNames = ParameterNameDiscovererCache.getParameterNameDiscoverer().getParameterNames(method);

                args = new Object[paramNames.length];
                Map<String, Object> paramMap = JsonUtil.fromJson(jsonContent, new TypeToken<Map<String, Object>>() {
                }.getType());
                for (int i = 0; i < paramNames.length; i++) {
                    Object tmpArg = paramMap.get(paramNames[i]);
                    if (tmpArg == null) {
                        args[i] = null;
                    } else {
                        args[i] = JsonUtil.fromJson(JsonUtil.toJson(tmpArg), inputTypes[i]);
                    }
                }

                TimerCheck.checkpoint("after param change 1");
            } else {
                Object arg0 = JsonUtil.fromJson(jsonContent, inputTypes[0]);
                args = new Object[]{arg0};

                TimerCheck.checkpoint("after param change 2");
            }
        }

        TimerCheck.checkpoint("after param change");

        //参数校验
        // checkParams(args, 1);

        Object result = null;

        log.info("invoke class:{}, method: {}, param: {}", target.getClass().getName(), method.getName(), jsonContent);

        result = method.invoke(target, args);

        return result;

    }


}
