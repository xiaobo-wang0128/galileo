package org.armada.galileo.rainbow_gate.transfer.gate_point.app_server;

import lombok.extern.slf4j.Slf4j;
import org.armada.galileo.annotation.openapi.AppId;
import org.armada.galileo.common.util.CommonUtil;
import org.armada.galileo.common.util.JsonUtil;
import org.armada.galileo.common.util.TimerCheck;
import org.armada.galileo.exception.BizException;
import org.armada.galileo.rainbow_gate.transfer.open_api.InvokeObject;
import org.armada.galileo.rainbow_gate.transfer.open_api.OpenApiContext;
import org.armada.galileo.rainbow_gate.transfer.constant.HttpOpenConstant;
import org.armada.galileo.rainbow_gate.transfer.domain.HttpJsonResponseBody;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Slf4j
public class HttpJsonServer extends HttpServlet {

    private static final long serialVersionUID = 1L;

    /**
     * api_code : invoke_object
     */
    private static Map<String, InvokeObject> targetObjectMap = new HashMap<>();

    /**
     * 拦截器
     */
    private static HttpJsonApiInterceptor apiInterceptor;


    public HttpJsonServer(Map<String, InvokeObject> targetObjectMap, HttpJsonApiInterceptor apiInterceptor) {
        HttpJsonServer.targetObjectMap = targetObjectMap;
        HttpJsonServer.apiInterceptor = apiInterceptor;
    }

    public HttpJsonServer(Map<String, InvokeObject> targetObjectMap) {
        HttpJsonServer.targetObjectMap = targetObjectMap;
    }

    public static Map<String, InvokeObject> getTargetObjectMap() {
        return targetObjectMap;
    }

    private static Executor executor = Executors.newFixedThreadPool(20);


    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doExecute(request, response);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        throw new RuntimeException("not support");
    }

    public void doExecute(HttpServletRequest request, HttpServletResponse response) {

        // 前置基础校验
        String apiCode = request.getHeader(HttpOpenConstant.ApiCodeHttpHeader);
        // String appId = request.getHeader(HttpOpenConstant.AppIdHeader);

//        String agentId = request.getHeader(HttpOpenConstant.AgentIdHeader);
//        String platCode = request.getHeader(HttpOpenConstant.PlatCodeHttpHeader);
//        String platName = request.getHeader(HttpOpenConstant.PlatNameHttpHeader);

        String apiContext = request.getHeader(HttpOpenConstant.API_CONTEXT_HEADER);
        apiContext = new String(CommonUtil.base64Decode(apiContext), StandardCharsets.UTF_8);

        HttpJsonResponseBody responseBody = new HttpJsonResponseBody();

        long l1 = System.currentTimeMillis();
        // TimerCheck.start();
        try {

            if (apiCode == null) {
                throw new BizException("接口不存在 " + apiCode);
            }

            InvokeObject invokeObject = targetObjectMap.get(apiCode);

            if (invokeObject == null) {
                throw new BizException("接口不存在 " + apiCode);
            }

            byte[] inputBytes = CommonUtil.readJsonFormBytes(request);

            String jsonContent = new String(inputBytes, StandardCharsets.UTF_8);

            // TimerCheck.checkpoint("收集参数");

            Object obj = executeRequest(invokeObject, apiContext, jsonContent);

            // TimerCheck.checkpoint("执行参数");

            responseBody.setCode(0);
            responseBody.setData(obj);

        } catch (Exception e) {

            log.error(e.getMessage(), e);

            responseBody.setCode(500);
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

            String msg = e.getMessage();

            if (e instanceof BizException) {
                BizException biz = (BizException) e;
                if (biz.getError() != null) {
                    responseBody.setErrorCode(biz.getError().toString());
                }
                responseBody.setMessage(biz.getMessage());
            } else {

                if (CommonUtil.isNotEmpty(msg)) {
                    responseBody.setMessage(msg);
                } else {
                    responseBody.setMessage("服务端执行异常");
                }

            }


        } finally {

            byte[] out = JsonUtil.toJson(responseBody).getBytes(StandardCharsets.UTF_8);

            try {
                response.getOutputStream().write(out);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }

            long l2 = System.currentTimeMillis();

            log.info("cost: " + (l2 - l1) + "ms");
        }


    }


    private static String Open_Api_Context = OpenApiContext.class.getName();

    public Object executeRequest(InvokeObject invokeObject, String apiContextJson, String jsonContent) throws Exception {

        if (invokeObject == null) {
            throw new BizException("接口地址不存在");
        }

        OpenApiContext openApiContext = JsonUtil.fromJson(apiContextJson, OpenApiContext.class);

        Method method = invokeObject.getMethod();

        Object target = invokeObject.getTarget();

        Type[] inputTypes = method.getGenericParameterTypes();

        Object[] args = new Object[inputTypes.length];

        // TimerCheck.checkpoint("before param change");
        if (inputTypes != null && inputTypes.length > 0) {

            // 多个参数 需要从map中 找到对应值
            if (inputTypes.length > 1) {

                // 需要参与反序列化的参数
                for (int i = 0; i < inputTypes.length; i++) {

                    Type parameterType = inputTypes[i];

                    // appid
                    if (method.getParameters()[i].isAnnotationPresent(AppId.class)) {
                        args[i] = openApiContext.getAppId();
                        continue;
                    }

                    // 上下文对象
                    else if (parameterType.getTypeName().equals(Open_Api_Context)) {
                        args[i] = openApiContext;
                        continue;
                    } else {
                        // 数据参数对象
                        try {
                            if (CommonUtil.isEmpty(jsonContent)) {
                                Class clz = Class.forName(inputTypes[i].getTypeName());
                                args[i] = clz.newInstance();
                            } else {
                                args[i] = JsonUtil.fromJson(jsonContent, inputTypes[i]);
                            }
                        } catch (Exception e) {
                            throw new BizException("输入参数格式不正确, json转换异常");
                        }
                    }

                }

                // TimerCheck.checkpoint("after param change 1");

            } else {

                Type parameterType = inputTypes[0];
                // 上下文对象
                if (parameterType.getTypeName().equals(Open_Api_Context)) {
                    args = new Object[]{openApiContext};
                } else {
                    Object arg0 = JsonUtil.fromJson(jsonContent, inputTypes[0]);
                    args = new Object[]{arg0};
                }

                // TimerCheck.checkpoint("after param change 2");
            }
        }

        // TimerCheck.checkpoint("after param change");

        Object result = null;

        log.info("invoke class:{}, method:{}, context:{}, inputJson:{}",
                target.getClass().getName(),
                method.getName(),
                apiContextJson,
                jsonContent);

        if (apiInterceptor != null) {
            apiInterceptor.before(openApiContext, target, method, args);
        }

        result = method.invoke(target, args);

        if (apiInterceptor != null) {
            apiInterceptor.after(openApiContext, target, method, args, result);
        }

        return result;

    }


}
