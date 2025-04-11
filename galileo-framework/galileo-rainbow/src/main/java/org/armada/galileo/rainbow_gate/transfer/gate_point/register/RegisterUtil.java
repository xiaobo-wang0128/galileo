package org.armada.galileo.rainbow_gate.transfer.gate_point.register;

import org.armada.galileo.common.util.CommonUtil;
import org.armada.galileo.rainbow_gate.transfer.discovery.RainbowDiscoveryFacade;
import org.armada.galileo.rainbow_gate.transfer.gate_point.app_client.AppClient;
import org.armada.galileo.rainbow_gate.transfer.gate_point.app_client.AppClient.ConnectType;
import org.armada.galileo.rainbow_gate.transfer.gate_point.app_server.AppServer;
import org.armada.galileo.rainbow_gate.transfer.gate_point.app_server.RainbowRpcInit;
import org.armada.galileo.rainbow_gate.transfer.interceptor.RainbowInterceptor;
import org.springframework.boot.web.servlet.ServletRegistrationBean;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * 配置样例类<br/>
 * <p>
 * 由于真实场景一台服务器既是服务端，又是客户端，所以 一台服务可能同时是 appServer和 appClient， 或者同时是 raninbowServer 和 rainbowClient<br/>
 * 配置示例
 *
 * @author xiaobowang
 */
public class RegisterUtil {

//    /**
//     * RainbowClient Servlet监听地址， 供 AppClient调用
//     */
//    public static final String RainbowClientAddress = "/rainbow/rain_client/call.action";

    /**
     * RainbowClient Servlet监听地址， 供 AppClient调用
     */
    public static final String AppServerAddress = "/rainbow/app_server/call.action";


    // ------------------- 初始化 appClient -------------------


    // ------------------- 初始化 appServer -------------------

    /**
     * 生成 appServer proxy
     */
    public static ServletRegistrationBean getAppServerServlet() {
        return new ServletRegistrationBean(new AppServer(), RegisterUtil.AppServerAddress);
    }

    /**
     * 注册接口地址信息至zookeeper
     *
     * @return
     */
    public static RainbowRpcInit getAppServiceRegister() {
        return new RainbowRpcInit();
    }

    // ------------------- rainbowServer 端注册监听服务 -------------------

    /**
     * 创建动态代理实例
     *
     * @param type        接口
     * @param connectType 连接类型
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> T getAppClientProxy(Class<T> type, ConnectType connectType) {

        RainbowDiscoveryFacade.registServiceConsumer(type, connectType);

        AppClient handler = new AppClient(type, connectType);

        return (T) Proxy.newProxyInstance(AppClient.class.getClassLoader(), new Class<?>[]{type}, handler);
    }

    /**
     * 创建直连代理实例
     *
     * @param type
     * @param httpServiceAddress 支持2种格式，例: http://192.168.1.1:8080  或  http://192.168.1.1:8080/xxx/xxxx.action ，不要以 / 结尾
     * @return
     * @author Wang Xiaobo 2019年11月30日
     */
    @SuppressWarnings("unchecked")
    public static <T> T getDirectAppClientProxy(Class<T> type, String httpServiceAddress) {

        String targetAddress = null;

        if (httpServiceAddress.indexOf(".action") != -1) {
            targetAddress = httpServiceAddress;
        } else {
            targetAddress = httpServiceAddress + AppServerAddress;
        }

        AppClient handler = new AppClient(type, targetAddress);

//        AppClient.registAppClientServiceName(type.getName());
//        AppClient.cacheAppServerAddress(type.getName(), CommonUtil.asList(targetAddress));


        AppClient.cacheRemoteAppServerAddress(type.getName(), targetAddress);

        return (T) Proxy.newProxyInstance(AppClient.class.getClassLoader(), new Class<?>[]{type}, handler);

    }

    public static void registConsumerInterceptor(RainbowInterceptor consumerInterceptor) {
        AppClient.setConsumerInterceptor(consumerInterceptor);
    }

    public static void registProviderInterceptor(RainbowInterceptor providerInterceptor) {
        AppServer.setProviderInterceptor(providerInterceptor);
    }

    // 当前服务

    /**
     * 记录当前服务部署的ip地址
     */
    private static List<String> currentAppAddress = new ArrayList<>();

    public static void setCurrentAppAddress(List<String> currentAppAddress) {
        RegisterUtil.currentAppAddress = currentAppAddress;
    }

    public static List<String> getCurrentAppAddress() {
        return currentAppAddress;
    }


    /**
     * 缓存所有服务的ip地址
     */
    private static Map<String, List<String>> allAppAddress = new HashMap<>();

    public static Map<String, List<String>> getAllAppAddress() {
        return allAppAddress;
    }

    public static void setAllAppAddress(Map<String, List<String>> allAppAddress) {
        RegisterUtil.allAppAddress = allAppAddress;
    }


}
