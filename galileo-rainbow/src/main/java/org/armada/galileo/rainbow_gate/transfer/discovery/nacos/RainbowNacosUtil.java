package org.armada.galileo.rainbow_gate.transfer.discovery.nacos;

import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.listener.Event;
import com.alibaba.nacos.api.naming.listener.EventListener;
import com.alibaba.nacos.api.naming.listener.NamingEvent;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.armada.galileo.common.util.CommonUtil;
import org.armada.galileo.common.util.JsonUtil;
import org.armada.galileo.rainbow_gate.transfer.gate_point.app_client.AppClient;
import org.armada.galileo.rainbow_gate.transfer.gate_point.register.RegisterUtil;

import java.io.Closeable;
import java.util.*;

/**
 * @author xiaobo
 * @date 2021/12/29 6:05 下午
 */
@Slf4j
public class RainbowNacosUtil {

    private static NamingService naming;

    public static void init(String nacosAddress) {
        Properties properties = new Properties();
        properties.setProperty("serverAddr", nacosAddress);

        while (true) {
            try {
                naming = NamingFactory.createNamingService(properties);
                break;
            } catch (Exception e) {
                log.error("nacos 连接失败: " + nacosAddress + ", msg:" + e.getMessage(), e);
                try {
                    Thread.sleep(3000);
                } catch (Exception ex) {
                }
            }
        }

        // 添加钩子
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            public void run() {
                if (naming != null) {
                    try {
                        log.info("[shutdown-hook] 主动关闭 nacos 连接");
                        naming.shutDown();
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                    }
                }
            }
        }));

    }

    public static void registerServiceProviders(String appName, List<String> interfaceNames, String ip, int httpPort) {

        try {
            Instance instance = new Instance();
            instance.setIp(ip);
            instance.setPort(httpPort);
            instance.setHealthy(true);
            instance.setWeight(1.0);
            instance.setClusterName("default");

            Map<String, String> meta = new LinkedHashMap<>();
            meta.put("app", appName);
            meta.put("service", JsonUtil.toJson(interfaceNames));
            instance.setMetadata(meta);

            log.info("[rainbow-nacos] 服务生产者注册成功, address: {}, services: {}", ip + ":" + httpPort, CommonUtil.join(interfaceNames, ", "));

            naming.registerInstance("galileo", instance);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

    }


    public static void watchRemoteServerChange(String currentAppName) {

        try {
            naming.subscribe("galileo", new EventListener() {

                @Override
                public void onEvent(Event event) {

                    NamingEvent ne = (NamingEvent) event;

                    Map<String, List<String>> serviceCache = new LinkedHashMap<>();

                    Map<String, List<String>> allAllAddress = new HashMap();

                    for (Instance instance : ne.getInstances()) {

                        String remoteIp = instance.getIp();
                        int remotePort = instance.getPort();

                        Map<String, String> meta = instance.getMetadata();
                        if (meta == null || meta.isEmpty()) {
                            continue;
                        }

                        String appName = meta.get("app");

                        List<String> appAddressList = allAllAddress.get(appName);
                        if (appAddressList == null) {
                            appAddressList = new ArrayList<>();
                            allAllAddress.put(appName, appAddressList);
                        }
                        appAddressList.add("http://" + remoteIp + ":" + remotePort);

                        String json = meta.get("service");
                        if (CommonUtil.isEmpty(json) || "[]".equals(json)) {
                            continue;
                        }

                        List<String> remoteServies = JsonUtil.fromJson(json, new TypeToken<List<String>>() {
                        }.getType());

                        if (remoteServies == null || remoteServies.isEmpty()) {
                            continue;
                        }

                        String localhostHttpAddress = "http://" + remoteIp + ":" + remotePort + RegisterUtil.AppServerAddress;

                        for (String serviceName : remoteServies) {

                            List<String> serviceUrls = serviceCache.get(serviceName);
                            if (serviceUrls == null) {
                                serviceUrls = new ArrayList<>();
                                serviceCache.put(serviceName, serviceUrls);
                            }
                            serviceUrls.add(localhostHttpAddress);
                        }
                    }

                    for (Map.Entry<String, List<String>> entry : serviceCache.entrySet()) {
                        AppClient.cacheAppServerAddress(entry.getKey(), entry.getValue());
                    }

                    RegisterUtil.setAllAppAddress(allAllAddress);
                    RegisterUtil.setCurrentAppAddress(allAllAddress.get(currentAppName));

                }
            });
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

//
//    @Data
//    @Accessors
//    @AllArgsConstructor
//    private static class Tmp {
//
//        private String serviceName;
//
//        private String url;
//    }
//
//
//    public static void main(String[] args) throws Exception {
//        Properties properties = new Properties();
//        properties.setProperty("serverAddr", "bronze-nacos:8848");
//        // properties.setProperty("namespace", "test_name_sapce");
//
//        NamingService naming = NamingFactory.createNamingService(properties);
//
//        String ip = CommonUtil.getRandomNumber(2) + "." + CommonUtil.getRandomNumber(2) + "." + CommonUtil.getRandomNumber(2) + "." + CommonUtil.getRandomNumber(2);
//
//        naming.registerInstance("com.cccc", ip, 8888, "default");
//
//        // naming = NamingFactory.createNamingService(properties);
//
//        naming.registerInstance("com.dddd", ip, 9999, "default");
//
//        System.out.println("##############");
//        System.out.println(naming.getAllInstances("com.xxxx"));
//
//        //naming.deregisterInstance("com.aaa", ip, 9999, "DEFAULT");
//
//        System.out.println("##############");
//        System.out.println(naming.getAllInstances("com.aaa"));
//
//
//        naming.subscribe("com.aaa", new EventListener() {
//            @Override
//            public void onEvent(Event event) {
//                System.out.println(JsonUtil.toJson(event));
//                System.out.println(((NamingEvent) event).getServiceName());
//                System.out.println(((NamingEvent) event).getInstances());
//            }
//        });
//
//        Thread.sleep(Integer.MAX_VALUE);
//    }


}
