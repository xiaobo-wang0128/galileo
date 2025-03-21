package org.armada.galileo.rainbow_gate.transfer.discovery;

import lombok.extern.slf4j.Slf4j;
import org.armada.galileo.common.loader.SpringMvcUtil;
import org.armada.galileo.common.util.CommonUtil;
import org.armada.galileo.exception.BizException;
import org.armada.galileo.annotation.rainbow.RainbowService;
import org.armada.galileo.rainbow_gate.transfer.constant.RainbowGateConfig;
import org.armada.galileo.rainbow_gate.transfer.discovery.nacos.RainbowNacosUtil;
import org.armada.galileo.rainbow_gate.transfer.discovery.zk.ChangeListener;
import org.armada.galileo.rainbow_gate.transfer.discovery.zk.ZkCLient;
import org.armada.galileo.rainbow_gate.transfer.domain.data.AppDeploy;
import org.armada.galileo.rainbow_gate.transfer.gate_point.app_client.AppClient;
import org.armada.galileo.rainbow_gate.transfer.gate_point.register.RegisterUtil;
import org.armada.galileo.rainbow_gate.transfer.discovery.domain.AppServiceDomain.Provider;
import org.armada.galileo.rainbow_gate.transfer.util.RainbowException;
import org.armada.galileo.common.util.SpringConfig;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author xiaobo
 * @date 2021/12/29 5:57 下午
 */
@Slf4j
public class RainbowDiscoveryFacade {

    private static final String nacos = "nacos";

    private static final String zookeeper = "zookeeper";

    private static String curretnProtcal = "";

    public static void init(String appName, String configCenterUrl) {

        if (CommonUtil.isEmpty(configCenterUrl)) {
            throw new BizException("注册中心地址不能为空");
        }
        if (configCenterUrl.startsWith(nacos)) {
            curretnProtcal = new String(nacos);
        } else if (configCenterUrl.startsWith(zookeeper)) {

            curretnProtcal = new String(zookeeper);

            String zkAddress = configCenterUrl.substring("zookeeper://".length());

            ZkCLient.init(zkAddress);

        } else {
            throw new BizException("注册中心地址格式不正确，示例： nacos://0.0.0.0:8848  或 zookeeper://0.0.0.0:2181 ");
        }

        ApplicationContext applicationContext = SpringMvcUtil.getContext();

        Map<String, Object> beanMap = applicationContext.getBeansWithAnnotation(RainbowService.class);

        List<String> beanNames = new ArrayList<String>();
        for (Map.Entry<String, Object> entry : beanMap.entrySet()) {
            beanNames.add(entry.getKey());
        }

        List<String> serviceNames = new ArrayList<>();
        for (String beanName : beanNames) {

            if (beanName.indexOf("org.springframework") != -1) {
                continue;
            }

            Class<?> type = applicationContext.getType(beanName);

            String typeName = type.getName();

            // spring 代理类
            if (typeName.indexOf("$") != -1) {
                typeName = typeName.substring(0, typeName.indexOf("$"));
                try {
                    type = Class.forName(typeName);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }

            if (type.isAnnotationPresent(RainbowService.class)) {

                Class<?>[] define = type.getInterfaces();

                for (Class<?> cls : define) {

                    if (cls.getName().indexOf("org.springframework") != -1) {
                        continue;
                    }

                    if (cls.getName().startsWith("provider")) {
                        throw new RainbowException("接口名不能以 provider 开头 ");
                    }

                    serviceNames.add(cls.getName());
                }
            }
        }


        String[] address = LocalServerAddressUtil.getLocalServerIpAndPort();


        //  nacos://0.0.0.0:8848  或 zookeeper://0.0.0.0:2181
        // nacos
        if (curretnProtcal.equals(nacos)) {

            String nacosAddress = configCenterUrl.substring("nacos://".length());

            RainbowNacosUtil.init(nacosAddress);

            RainbowNacosUtil.registerServiceProviders(appName, serviceNames, address[0], Integer.valueOf(address[1]));

            new Thread(() -> {
                RainbowNacosUtil.watchRemoteServerChange(appName);
            }).start();

        }
        // zookeeper
        else if (curretnProtcal.equals(zookeeper)) {

            RainbowGateConfig.setAppName(appName);

            String zkAddress = configCenterUrl.substring("zookeeper://".length());

            ZkCLient.init(zkAddress);

            RainbowDiscoveryFacade.registerServiceProviders(serviceNames, address[0], Integer.valueOf(address[1]));

            RainbowDiscoveryFacade.listenAppServerAddress4AppClient();

            RainbowDiscoveryFacade.listenGateClientAddress();
        }
    }

    private static void registerServiceProviders(List<String> interfaceNames, String httpHost, int httpPort) {

        String localhostAddress = "http://" + httpHost + ":" + httpPort;
        String localhostHttpAddress = "http://" + httpHost + ":" + httpPort + RegisterUtil.AppServerAddress + "?appName=" + RainbowGateConfig.getAppName();

        for (String interfaceName : interfaceNames) {
            ZkCLient.registerServiceProvider(interfaceName, localhostHttpAddress);
        }

        String hostName = localhostAddress;
        String deployUrl = localhostAddress;
        String pid = System.getProperty("PID");
        String userHome = System.getProperty("user.name");
        String userDir = SpringConfig.getAppHomePath();
        String appStartTime = CommonUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss");
        String osName = System.getProperty("os.name");

        AppDeploy ap = new AppDeploy();
        ap.setAppName(RainbowGateConfig.getAppName());
        ap.setAppDeployPath(userDir);
        ap.setHostName(hostName);
        ap.setPid(pid);
        ap.setUserHome(userHome);
        ap.setDeployUrl(deployUrl);
        ap.setAppStartTime(appStartTime);
        ap.setOsName(osName);

        ZkCLient.registerAppDeploy(RainbowGateConfig.getAppName(), ap);

    }

    public static void registServiceConsumer(Class<?> type, AppClient.ConnectType connectType) {

//        if (connectType == ConnectType.direct || connectType == ConnectType.direct_broadcast) {
//            AppClient.registAppClientServiceName(type.getName());
//        }

        AppClient.registAppClientServiceName(type.getName());

        if (curretnProtcal.equals(zookeeper)) {

            String localhostHttpAddress = LocalServerAddressUtil.getLocalServerAddress() + "/?appName=" + RainbowGateConfig.getAppName() + "&connectType=" + connectType;

            ZkCLient.registServiceCusotmer(type.getName(), localhostHttpAddress);

            Provider provider = ZkCLient.getProviderFromZK(type.getName());
            if (provider != null && provider.getAddressList() != null) {
                AppClient.cacheAppServerAddress(provider.getServiceName(), provider.getAddressList());
            }

        }
    }

//    public static void initGateClient() {
//        ZkCLient.registerRainbowClient(LocalServerAddressUtil.getLocalServerAddress() + RegisterUtil.RainbowClientAddress);
//    }

    public static void listenAppServerAddress4AppClient() {
        ZkCLient.listenAppServerAddress("appClient", new ChangeListener() {

            public void afterChange(String className, List<String> newChildPaths) {

                // app-server 下缓存的服务列表， 可能不是当前应用需要的， 如果不需要则忽略
                try {
                    Class.forName(className);
                } catch (Exception e) {
                    return;
                }

                AppClient.cacheAppServerAddress(className, newChildPaths);
            }
        });
    }

    public static void listenGateClientAddress() {
        ZkCLient.listenGateClientAddress();
    }
}
