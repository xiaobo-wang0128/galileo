package org.armada.galileo.rainbow_gate.transfer.gate_point.app_client;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.armada.galileo.annotation.openapi.*;
import org.armada.galileo.common.loader.SpringMvcUtil;
import org.armada.galileo.common.util.CommonUtil;
import org.armada.galileo.common.util.HttpUtil;
import org.armada.galileo.common.util.JsonUtil;
import org.armada.galileo.exception.BizException;
import org.armada.galileo.annotation.rainbow.RouteKey;
import org.armada.galileo.rainbow_gate.transfer.open_api.InvokeObject;
import org.armada.galileo.rainbow_gate.transfer.connection.http.HttpPostUtil;
import org.armada.galileo.rainbow_gate.transfer.constant.GateConstant;
import org.armada.galileo.rainbow_gate.transfer.constant.HttpOpenConstant;
import org.armada.galileo.rainbow_gate.transfer.discovery.LocalServerAddressUtil;
import org.armada.galileo.rainbow_gate.transfer.domain.protocol.AppRequestDomain;
import org.armada.galileo.rainbow_gate.transfer.domain.protocol.AppResponseDomain;
import org.armada.galileo.rainbow_gate.transfer.domain.protocol.RainbowRequestType;
import org.armada.galileo.rainbow_gate.transfer.gate_codec.GateCodecUtil;
import org.armada.galileo.rainbow_gate.transfer.gate_point.app_server.AppServer;
import org.armada.galileo.rainbow_gate.transfer.gate_point.app_server.HttpJsonServer;
import org.armada.galileo.rainbow_gate.transfer.gate_point.app_server.RainbowRpcInit;
import org.armada.galileo.rainbow_gate.transfer.gate_point.register.RegisterUtil;
import org.armada.galileo.rainbow_gate.transfer.interceptor.RainbowInterceptor;
import org.armada.galileo.rainbow_gate.transfer.open_api.OpenApiContext;
import org.armada.galileo.rainbow_gate.transfer.util.RainbowException;
import org.armada.galileo.rainbow_gate.transfer.util.RainbowUtil;
import org.slf4j.LoggerFactory;
import org.springframework.util.ReflectionUtils;

/**
 * http 远程通信实现
 *
 * @author xiaobowang 2019年4月18日
 */
public class AppClient implements InvocationHandler {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(AppClient.class);

    private Class<?> cls;

    private static List<String> gateClientAddressList;

    /**
     * 消费端远程地址缓存
     */
    private static ConcurrentHashMap<String, List<String>> customerServerServiceAddreassList = new ConcurrentHashMap<String, List<String>>();

    /**
     * zk中所有地址缓存
     */
    private static ConcurrentHashMap<String, List<String>> allAppServerServiceAddreassList = new ConcurrentHashMap<String, List<String>>();

    /**
     * 远程接口名缓存
     */
    private static List<String> customerServiceNameList = new CopyOnWriteArrayList<>();

    /**
     * 拦截器
     */
    private static org.armada.galileo.rainbow_gate.transfer.interceptor.RainbowInterceptor rainbowInterceptor = null;

    /**
     * 获取直连模式所有服务生产者的地址
     *
     * @param interfaceName 接口名
     * @return
     * @author Wang Xiaobo 2020年2月13日
     */
    public static List<String> getDirectProviderServerAddress(String interfaceName) {
        return allAppServerServiceAddreassList.get(interfaceName);
    }

    /**
     * 监听并缓存appServer端接口地址，只缓存声明过 http 直连类型的接口名
     *
     * @param serviceClassName
     * @param appServerAddressList
     */
    public static void cacheAppServerAddress(String serviceClassName, List<String> appServerAddressList) {

        if (appServerAddressList == null || appServerAddressList.isEmpty()) {

            log.info("[app-client] remote app-server interface seems to be offline, interface: {}", serviceClassName);

            allAppServerServiceAddreassList.remove(serviceClassName);
            if (customerServerServiceAddreassList.get(serviceClassName) != null) {
                customerServerServiceAddreassList.remove(serviceClassName);
                log.info("[app-client] remote app-server interface is offline, interface: {}", serviceClassName);
            }
            return;
        }

        allAppServerServiceAddreassList.put(serviceClassName, appServerAddressList);

        if (customerServiceNameList.contains(serviceClassName)) {
            if (customerServerServiceAddreassList.get(serviceClassName) != null) {
                if (RainbowUtil.equals(customerServerServiceAddreassList.get(serviceClassName), appServerAddressList)) {
                    return;
                }
            }
            customerServerServiceAddreassList.put(serviceClassName, appServerAddressList);
            log.info("[app-client] cache remote app-server interface: {}, address: {}", serviceClassName, JsonUtil.toJson(appServerAddressList));
        }
    }

    public static void registAppClientServiceName(String serviceClassName) {

        customerServiceNameList.add(serviceClassName);
        if (allAppServerServiceAddreassList.get(serviceClassName) != null) {
            customerServerServiceAddreassList.put(serviceClassName, allAppServerServiceAddreassList.get(serviceClassName));
            log.info("[app-client] cache remote app-server interface: {}, address: {}", serviceClassName, JsonUtil.toJson(allAppServerServiceAddreassList.get(serviceClassName)));
        }
    }

    private static AtomicInteger atomicIndexGate = new AtomicInteger(0);



    private static Map<String, String> directRemoteAddress = new HashMap();

    public static void cacheRemoteAppServerAddress(String className, String address) {
        directRemoteAddress.put(className, address);
    }


    public static String getDirectRemoteAddress(String className){
        return directRemoteAddress.get(className);
    }


    // private static AtomicInteger atomicIndexDirect = new AtomicInteger(0);

    public static enum ConnectType {

        /**
         * 网关模式
         */
        @Deprecated gate,

        /**
         * 直连模式
         */
        direct,

        /**
         * 直接广播式调用，向所有服务生产者发起调用
         */
        direct_broadcast

    }

    public AppClient(Class<?> cls, ConnectType connectType) {
        this.cls = cls;
        this.connectType = connectType;

    }

    /**
     * 定向http直连模式
     */
    private String directHttpUrl;

    public AppClient(Class<?> cls, String directHttpUrl) {
        this.cls = cls;
        this.connectType = ConnectType.direct;
        this.directHttpUrl = directHttpUrl;
    }

    public AppClient() {

    }

    public static void registerGateClientAddress(List<String> rainbowAddressList) {

        synchronized (log) {

            if (rainbowAddressList == null || rainbowAddressList.size() == 0) {
                log.info("[app-client] remote gate-client is offline");
            } else {
                if (RainbowUtil.equals(rainbowAddressList, AppClient.gateClientAddressList)) {
                    return;
                }

                log.info("[app-client] cache remote gate-client address: {}", JsonUtil.toJson(rainbowAddressList));
            }

            AppClient.gateClientAddressList = rainbowAddressList;

        }

    }

    private ConnectType connectType;

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

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        if (objectMethodNames.contains(method.getName())) {
            return method.invoke(this, args);
        }

        Object resultObj = null;
        AppRequestDomain appRequestDomain = null;
        AppResponseDomain appResponseDomain = null;

        long l1 = System.currentTimeMillis();

        /** ---- step 1: 构造反映调用所需参数 ------ */

        String className = cls.getName();
        String methodName = method.getName();

        String[] paramTypeNames = null;
        Object routeKey = null;
        Parameter[] methodParameters = method.getParameters();

        boolean async = false;
        String group = null;
        String apiCode = null;
        String appId = null;
        if (method.isAnnotationPresent(OpenApiMethod.class)) {
            OpenApiMethod openApiMethod = method.getAnnotation(OpenApiMethod.class);
            apiCode = openApiMethod.code();
            async = openApiMethod.async();
            group = openApiMethod.group();
        }

        if (methodParameters != null && methodParameters.length > 0) {

            paramTypeNames = new String[methodParameters.length];

            for (int i = 0; i < methodParameters.length; i++) {

                if (methodParameters[i].isAnnotationPresent(RouteKey.class)) {
                    routeKey = (args[i]);
                    if (routeKey == null) {
                        throw new RainbowException("标记为 @RouteKey 的字段值不能为空");
                    }
                }

                if (apiCode != null && methodParameters[i].isAnnotationPresent(AppId.class) && args[i] != null) {
                    appId = args[i].toString();
                }

                paramTypeNames[i] = methodParameters[i].getType().getName();
            }
        }

        // 请求号
        String requestNo = UUID.randomUUID().toString();
        appRequestDomain = new AppRequestDomain();
        appRequestDomain.setRequestType(RainbowRequestType.Request);
        appRequestDomain.setRequestNo(requestNo);
        appRequestDomain.setClassName(className);
        appRequestDomain.setMethodName(methodName);
        appRequestDomain.setParamTypeNames(paramTypeNames);
        appRequestDomain.setParamInputs(args);
        if (routeKey != null) {
            appRequestDomain.setRouteKey(routeKey.toString());
        }

        if (rainbowInterceptor != null) {
            rainbowInterceptor.before(appRequestDomain);
        }

        if (connectType == ConnectType.direct) {
            appResponseDomain = requestDirect(method, appRequestDomain, appId, apiCode, group, async);
        } else if (connectType == ConnectType.gate) {
            appResponseDomain = requesGate(appRequestDomain);
        } else if (connectType == ConnectType.direct_broadcast) {
            appResponseDomain = requestDirectBroadcast(appRequestDomain);
        }

        if (appResponseDomain == null) {
            throw new RainbowException("远程调用异常");
        }

        if (appResponseDomain != null && appResponseDomain.getResult() != null) {
            resultObj = appResponseDomain.getResult();
        }
        // 计算请求速度
        long l2 = System.currentTimeMillis();

        if (appResponseDomain.getRemoteIp() != null) {
            log.info("[app-client] rainbow-rpc execute done, remoteIp:{} interface:{}, methodName:{}, cost:{}ms", appResponseDomain.getRemoteIp(), className, methodName, (l2 - l1));
        } else {
            log.info("[app-client] rainbow-rpc execute done, interface:{}, methodName:{}, cost:{}ms", className, methodName, (l2 - l1));
        }

        if (rainbowInterceptor != null) {
            rainbowInterceptor.complete(appRequestDomain, appResponseDomain);
        }
        return resultObj;
    }

    private static Map<String, String> routeMap = new HashMap<>();

    /**
     * 设置路同地址
     *
     * @param routeKey
     * @param requestUrl inner 表示通过内网注册中路由， ip地址 表示直连ip
     */
    public static void putRoutUrl(String routeKey, String requestUrl) {

        if ("inner".equals(requestUrl)) {
            routeMap.put(routeKey, "inner");
            return;
        }

        if (!requestUrl.startsWith("http://") && !requestUrl.startsWith("https://")) {
            throw new RainbowException("地址必须以 http / https 开头");
        }

        if (requestUrl.endsWith("/")) {
            requestUrl = requestUrl.substring(0, requestUrl.length() - 1);
        }

        routeMap.put(routeKey, requestUrl + RegisterUtil.AppServerAddress);
    }


    /**
     * 请求 appServer， 直接广播式调用，向所有服务生产者发起调用
     *
     * @param domain
     * @return
     */
    public static AppResponseDomain requestDirectBroadcast(AppRequestDomain domain) {

        List<String> appServerAddressList = customerServerServiceAddreassList.get(domain.getClassName());
        if (appServerAddressList == null || appServerAddressList.isEmpty()) {
            throw new RainbowException("[app-client] error, 还没有注册 appServer地址, interface: " + domain.getClassName());
        }

        String localIpPort = LocalServerAddressUtil.getLocalServerAddress();

        AppResponseDomain mainResult = new AppResponseDomain(0);
        List<Object> resultList = new ArrayList<>();
        byte[] data = GateCodecUtil.encodeRequest(domain);
        for (String address : appServerAddressList) {

            String httpUrl = address;

            Object requestResult = null;
            // 本地调用
            if (httpUrl.startsWith(localIpPort)) {
                requestResult = invokeLocal(domain);
            }
            // 远程
            else {
                if (!httpUrl.endsWith(RegisterUtil.AppServerAddress)) {
                    httpUrl = httpUrl + RegisterUtil.AppServerAddress;
                }
                byte[] result = HttpPostUtil.request(httpUrl, null, data);
                AppResponseDomain itemResult = GateCodecUtil.decodeResponse(result);

                if (itemResult.getCode() != 0) {
                    throw new RainbowException("远程调用异常, url:" + address + ", service: " + domain.getClassName() + "#" + domain.getMethodName() + ", message:" + itemResult.getMessage());
                }
                requestResult = itemResult.getResult();
            }

            if (requestResult != null && requestResult.getClass().getTypeName().equals("java.util.ArrayList")) {
                if (requestResult.getClass().getTypeName().indexOf("List") != -1) {
                    resultList.addAll((List<?>) requestResult);
                }
            }
        }

        if (resultList.size() > 0) {
            mainResult.setResult(resultList);
        }

        return mainResult;

    }

    private static String Open_Api_Context = OpenApiContext.class.getName();


    /**
     * 请求 appServer， 直连模式
     *
     * @param domain
     * @return
     */
    public AppResponseDomain requestDirect(Method method, AppRequestDomain domain, String appId, String apiCode, String groupField, boolean async) {


        boolean isOpenApiResponse = false;
        if (method.getDeclaringClass().isAnnotationPresent(OpenApi.class)) {
            OpenApi cls = method.getDeclaringClass().getAnnotation(OpenApi.class);
            isOpenApiResponse = (cls.apiFrom() == ApiRole.KYB);
        }


        // 是否为 open api 的调用
        if (isOpenApiResponse) {

            // 没有appid， 表示是在页面上新增的数据， 无需回传
            if (appId == null) {
                AppResponseDomain responseDomain = new AppResponseDomain();
                responseDomain.setCode(0);
                log.info("[open-api-client] appId 为空, 将不会调用开放平台, apiCode: " + apiCode);
                return responseDomain;
            }
            // 发送请求至开放平台
            else {

                List<String> openPlatUrls = RegisterUtil.getAllAppAddress().get("open-api");

                if (CommonUtil.isEmpty(openPlatUrls)) {
                    throw new RainbowException("[open-api-client]  没有找到开放平台地址");
                }

                int index = (int) (Math.random() * openPlatUrls.size());
                String appServerAddress = openPlatUrls.get(index) + HttpOpenConstant.OpenApiServletPath;

                Type[] inputTypes = method.getGenericParameterTypes();

                // InvokeObject invokeObject = HttpJsonServer.getTargetObjectMap().get(apiCode);

                Map<String, String> headers = new HashMap<>();
                headers.put(HttpOpenConstant.AppIdHeader, appId);
                headers.put(HttpOpenConstant.ApiCodeHttpHeader, apiCode);
                headers.put(HttpOpenConstant.AppNameHttpHeader, RainbowRpcInit.appName);

                String postJson = null;

                Object[] args = domain.getParamInputs();

                // 需要参与反序列化的参数
                for (int i = 0; i < inputTypes.length; i++) {

                    Type parameterType = inputTypes[i];

                    // appid
                    if (method.getParameters()[i].isAnnotationPresent(AppId.class)) {
                        continue;
                    }

                    // 上下文对象
                    if (parameterType.getTypeName().equals(Open_Api_Context)) {
                        continue;
                    }

                    // 数据参数对象
                    postJson = JsonUtil.toJson(args[i]);
                    break;

                }

                String responseJson = HttpUtil.postJson(appServerAddress, postJson, headers, "utf-8", 60000);

                JsonObject jsonObject = (JsonObject) new JsonParser().parse(responseJson);

                JsonElement codeEl = jsonObject.get("code");

                if (codeEl == null) {
                    throw new RainbowException("[open-api-client] 开放平台返回的 json 缺少 code 标记: " + responseJson);
                }

                int code = codeEl.getAsInt();

                if (code != 0) {
                    String message = jsonObject.get("message").getAsString();
                    throw new RainbowException(message);
                }

                Class returnType = method.getReturnType();

                Object result = null;

                if (!returnType.getName().equals("void")) {
                    JsonElement dataEl = jsonObject.get("data");
                    if (dataEl != null) {
                        result = JsonUtil.fromJson(dataEl, returnType);
                    } else {
                        log.info("[open-api-client] 开放平台返回了空值: " + responseJson);
                    }
                }

                AppResponseDomain responseDomain = new AppResponseDomain();
                responseDomain.setCode(0);
                responseDomain.setResult(result);

                return responseDomain;

            }


        }


        if (async) {

            // 地址路由
            if (domain.getRouteKey() != null) {
                String routePath = routeMap.get(domain.getRouteKey());
                if (routePath == null) {
                    throw new RainbowException("[rainbow-direct-request] 没有找到路由地址, routeKey: " + domain.getRouteKey());
                }
            }

            // 找到回传分组的字段值
            String groupValue = null;

            if (CommonUtil.isNotEmpty(groupField)) {

                for (int i = 0; i < domain.getParamTypeNames().length; i++) {
                    String paramTypeName = domain.getParamTypeNames()[i];
                    if (paramTypeName.equals(groupField)) {
                        if (domain.getParamInputs()[i] != null) {
                            groupValue = domain.getParamInputs()[i].toString();
                        }
                        break;
                    }
                }

                if (groupValue == null) {
                    for (int i = 0; i < domain.getParamInputs().length; i++) {
                        Object obj = domain.getParamInputs()[i];
                        if (obj == null) {
                            continue;
                        }
                        if (obj.getClass().getName().startsWith("java.")) {
                            continue;
                        }
                        try {
                            Field field = null;
                            field = obj.getClass().getDeclaredField(groupField);
                            if (field != null) {
                                ReflectionUtils.makeAccessible(field);
                                Object v = ReflectionUtils.getField(field, obj);
                                if (v != null) {
                                    groupValue = v.toString();
                                }
                                break;
                            }
                        } catch (Exception e) {
                        }
                    }
                }

                if (groupValue == null) {
                    log.warn("[异步回传] 分组字段为空值, 接口:{}, 入参:{}", apiCode, JsonUtil.toJson(domain.getParamInputs()));
                }

                domain.setGroupValue(groupValue);

                // 如果当前服务部署了多台， 需要将当前消息路由到指定的机器， 让该机器发送异步消息给相应的服务， 以保障消息的顺序性
                List<String> currentServerAddress = RegisterUtil.getCurrentAppAddress();
                if (groupValue != null && currentServerAddress != null && currentServerAddress.size() > 1) {

                    long hasValue = Math.abs(CommonUtil.hash(groupValue));
                    int index = (int) (hasValue % currentServerAddress.size());

                    String targetUrl = currentServerAddress.get(index);
                    if (!targetUrl.equals(LocalServerAddressUtil.getLocalServerAddress())) {
                        domain.setRequestType(RainbowRequestType.Group_Route);
                        byte[] data = GateCodecUtil.encodeRequest(domain);
                        HttpPostUtil.request(targetUrl + RegisterUtil.AppServerAddress, null, data);
                        AppResponseDomain actionResult = new AppResponseDomain();
                        actionResult.setCode(0);
                        return actionResult;
                    }
                }
            }

            AsyncRequestCache.push(domain, apiCode);

            AppResponseDomain actionResult = new AppResponseDomain();
            actionResult.setCode(0);

            return actionResult;
        }

        // 同步调用
        String appServerAddress = null;

        // 指定地址
        if (this.directHttpUrl != null) {
            appServerAddress = this.directHttpUrl;
        } else {
            appServerAddress = getTargetRequestUrl(domain.getRouteKey(), domain.getClassName());
        }

        byte[] data = GateCodecUtil.encodeRequest(domain);

        byte[] result = HttpPostUtil.request(appServerAddress, null, data);

        AppResponseDomain actionResult = GateCodecUtil.decodeResponse(result);
        if (actionResult.getCode() != 0) {
            String tmp = appServerAddress;
            if (tmp.endsWith(RegisterUtil.AppServerAddress)) {
                tmp = tmp.substring(0, tmp.length() - RegisterUtil.AppServerAddress.length());
            }
            log.error("[app-client] rainbow-rpc response error，remote address:{}, remote error:{} ", tmp, actionResult.getMessage());
            throw new RainbowException(actionResult.getMessage());
        }
        if (appServerAddress != null) {
            int tmpIndex = appServerAddress.indexOf("//");
            if (tmpIndex != -1) {
                appServerAddress = appServerAddress.substring(tmpIndex + 2);
            }
            tmpIndex = appServerAddress.indexOf("/");
            if (tmpIndex != -1) {
                appServerAddress = appServerAddress.substring(0, tmpIndex);
            }

            actionResult.setRemoteIp(appServerAddress);
        }

        return actionResult;

    }


    public static String getTargetRequestUrl(String routeKey, String className) {

        // 地址路由
        if (routeKey != null) {
            String routePath = routeMap.get(routeKey);
            if (routePath == null) {
                throw new RainbowException("[rainbow-direct-request] 没有找到路由地址, routeKey: " + routeKey);
            }

            if (!"inner".equals(routePath)) {
                return routePath;
            }
        }

        String appServerAddress = null;

        // 注册中心
        List<String> appServerAddressList = customerServerServiceAddreassList.get(className);

        if (appServerAddressList == null || appServerAddressList.isEmpty()) {
            throw new RainbowException("[rainbow-direct-request] 没有获取到远程服务地址 className: " + className);
        }

        if (appServerAddressList.size() == 1) {
            appServerAddress = appServerAddressList.get(0);
        } else {
            int index = (int) (Math.random() * appServerAddressList.size());
            appServerAddress = appServerAddressList.get(index);
        }
        return appServerAddress;
    }


    /**
     * 请求 rainbowClient 网关模式
     *
     * @param domain
     * @return
     */
    public static AppResponseDomain requesGate(AppRequestDomain domain) {

        if (gateClientAddressList == null || gateClientAddressList.isEmpty()) {
            throw new RainbowException("[rainbow-gate-request] 没有远程 gate-client 地址");
        }

        String gateClientHttpAddress = null;
        if (gateClientAddressList.size() == 1) {
            gateClientHttpAddress = gateClientAddressList.get(0);
        } else {
            int index = atomicIndexGate.incrementAndGet();

            if (index > gateClientAddressList.size() - 1) {
                index = 0;
                atomicIndexGate.set(index);
            }

            gateClientHttpAddress = gateClientAddressList.get(index);
        }

        log.info("[rainbow-gate-request]  request gate-client: {}, routeKey: {}, type: {}, className: {}, method: {}, requestNo: {}", //
                gateClientHttpAddress, domain.getRouteKey(), domain.getRequestType(), domain.getClassName(), domain.getMethodName(), domain.getRequestNo());

        byte[] data = GateCodecUtil.encodeRequest(domain);

        byte[] result = HttpPostUtil.request(gateClientHttpAddress, null, data);

        // code ==1 表示请求已在执行中，需等待返回结果
        AppResponseDomain actionResult = GateCodecUtil.decodeResponse(result);
        if (actionResult.getCode() != 1) {
            throw new RainbowException(actionResult.getMessage());
        }

        AppRequestDomain checkDomain = new AppRequestDomain();
        checkDomain.setRequestNo(domain.getRequestNo());
        checkDomain.setRequestType(RainbowRequestType.Check);
        byte[] checkBytes = GateCodecUtil.encodeRequest(checkDomain);

        int waitTime = 0;
        while (true) {

            try {
                Thread.sleep(GateConstant.appClientSleepTime);

                waitTime += GateConstant.appClientSleepTime;

            } catch (Exception e) {
            }

            // log.info("[app-client] request gate-client: {}, routeKey: {}, type: {}, className: {}, method: {}, requestNo: {}", //
            // gateClientHttpAddress, domain.getRouteKey(), checkDomain.getRequestType(), domain.getClassName(), domain.getMethodName(), domain.getRequestNo());

            result = HttpPostUtil.request(gateClientHttpAddress, null, checkBytes);

            actionResult = GateCodecUtil.decodeResponse(result);

            if (actionResult == null) {
                log.error("[rainbow-gate-request] 远程调用异常, 返回空值, url: " + gateClientHttpAddress);
                throw new RainbowException("远程调用异常, 数据解析为空值，请检查网关通信");
            }

            if (actionResult.getCode() == 1) {
                if (waitTime >= GateConstant.appClientMaxWaitTime) {
                    log.error("[rainbow-gate-request] 远程调用异常, message:{}, url:{}", "远程接口调用超时", gateClientHttpAddress);
                    throw new RainbowException("[app-client] error: 远程接口调用超时");
                }
                continue;
            }

            if (actionResult.getCode() != 0) {
                log.error("[rainbow-gate-request] 远程调用异常, message:{}, url:{}", actionResult.getMessage(), gateClientHttpAddress);
                throw new RainbowException(actionResult.getMessage());
            }

            break;
        }

        result = null;
        checkBytes = null;

        return actionResult;
    }


    public static void setConsumerInterceptor(RainbowInterceptor rainbowInterceptor) {
        AppClient.rainbowInterceptor = rainbowInterceptor;
    }


    private static Object invokeLocal(AppRequestDomain appRequestDomain) {
        try {
            String className = appRequestDomain.getClassName();
            String methodName = appRequestDomain.getMethodName();
            String[] paramTypeNames = appRequestDomain.getParamTypeNames();
            Object[] paramInputs = appRequestDomain.getParamInputs();

            Class<?> cls = AppServer.getSimpleCLass(className); //   Class.forName(className);
            Class<?>[] inputTypes = null;
            if (paramTypeNames != null) {
                inputTypes = new Class<?>[paramTypeNames.length];
                for (int i = 0; i < paramTypeNames.length; i++) {
                    inputTypes[i] = AppServer.getSimpleCLass(paramTypeNames[i]); //   Class.forName(paramTypeNames[i]);
                }
            }

            java.lang.reflect.Method method = null;
            if (inputTypes != null && inputTypes.length > 0) {
                method = ReflectionUtils.findMethod(cls, methodName, inputTypes);
            } else {
                method = ReflectionUtils.findMethod(cls, methodName);
            }
            if (method == null) {
                throw new RuntimeException("没到找待执行方法: " + methodName);
            }

            Object targetObject = SpringMvcUtil.getBean(cls);

            if (targetObject == null) {
                throw new RainbowException("没有找到接口实现类");
            }

            Object result = null;

            // 执行目标方法
            if (paramInputs != null && paramInputs.length > 0) {
                result = ReflectionUtils.invokeMethod(method, targetObject, paramInputs);
            } else {
                result = ReflectionUtils.invokeMethod(method, targetObject);
            }

            return result;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new BizException(e);
        }
    }

}
