package org.armada.galileo.rainbow_gate.transfer.gate_point.app_client;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.extern.slf4j.Slf4j;
import org.armada.galileo.annotation.openapi.OpenApiMethod;
import org.armada.galileo.common.util.CommonUtil;
import org.armada.galileo.common.util.HttpUtil;
import org.armada.galileo.common.util.JsonUtil;
import org.armada.galileo.rainbow_gate.transfer.constant.HttpOpenConstant;
import org.armada.galileo.rainbow_gate.transfer.util.RainbowException;
import org.springframework.core.DefaultParameterNameDiscoverer;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * @author xiaobo
 * @date 2023/2/1 17:36
 */
@Slf4j
public class OpenApiHttpJsonClientTest implements InvocationHandler {

    private static Executor ex = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    private ApiUrlRouter apiUrlRouter;

    private String appId;

    private String appSecret;

    public static <T> T getProxy(Class<T> type, ApiUrlRouter apiUrlRouter) {
        OpenApiHttpJsonClientTest handler = new OpenApiHttpJsonClientTest();
        handler.apiUrlRouter = apiUrlRouter;
        return (T) Proxy.newProxyInstance(OpenApiHttpJsonClientTest.class.getClassLoader(), new Class<?>[]{type}, handler);
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

        return invokeMethod(method, args, false);

    }

    public Object invokeMethod(Method method, Object[] args, boolean isAsync) throws Throwable {
        // 回传给用户的 json
        String requestJson = toJson(method, args);

        String apiMethodCode = method.getAnnotation(OpenApiMethod.class).code();

        String requestUrl = apiUrlRouter.getUrl() + "/" + apiMethodCode;
        String appId = apiUrlRouter.getAppId();
        String appSecret = apiUrlRouter.getAppSecret();

        String timestamp = String.valueOf(System.currentTimeMillis());
        String sign = CommonUtil.md5(appSecret + timestamp + requestJson);

        Map<String, String> headers = new HashMap<>();
        headers.put(HttpOpenConstant.AppIdHeader, appId);
        headers.put(HttpOpenConstant.AppSignHeader, sign);
        headers.put(HttpOpenConstant.AppRequestTimeHeader, timestamp);

        // 请求客户服务器
        String responseJson = HttpUtil.postJson(requestUrl, requestJson, headers, "utf-8", 60000);

        JsonObject jsonObject = (JsonObject) new JsonParser().parse(responseJson);

        int code = jsonObject.get("code").getAsInt();

        if (code != 0) {
            String message = jsonObject.get("message").getAsString();
            throw new RainbowException(message);
        }

        Class returnType = method.getReturnType();

        if (returnType.getName().equals("void")) {
            return null;
        }

        return JsonUtil.fromJson(jsonObject.get("data"), returnType);
    }


    static DefaultParameterNameDiscoverer defaultParameterNameDiscoverer = new DefaultParameterNameDiscoverer();

    public static String toJson(Method method, Object[] args) {
        // 多个参数 需要从map中 找到对应值
        if (args == null || args.length == 0) {
            return null;
        }
        if (args.length == 1) {
            return JsonUtil.toJson(args[0]);
        }
        // 按参数名组装 json
        else {

            String[] paramNames = defaultParameterNameDiscoverer.getParameterNames(method);

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
