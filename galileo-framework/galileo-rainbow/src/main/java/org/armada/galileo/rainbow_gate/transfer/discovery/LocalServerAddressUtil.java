package org.armada.galileo.rainbow_gate.transfer.discovery;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.armada.galileo.common.util.CommonUtil;
import org.armada.galileo.common.util.JsonUtil;
import org.armada.galileo.common.util.SpringConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocalServerAddressUtil {

    private static Logger log = LoggerFactory.getLogger(LocalServerAddressUtil.class);

    public static String currentHttpAddress = null;

    public static String getLocalServerAddress() {

        String[] currentIpPort = getLocalServerIpAndPort();

        currentHttpAddress = "http://" + currentIpPort[0] + ":" + currentIpPort[1];

        return currentHttpAddress;
    }


    private static String[] currentIpPort = null;

    public static String[] getLocalServerIpAndPort() {
        if (currentIpPort != null) {
            return currentIpPort;
        }

        synchronized (log) {

            if (currentIpPort != null) {
                return currentIpPort;
            }

            String realPort = null;

            Map<String, String> appStartConfig = getConfig();

            // 用于兼容 k8s docker 内部端口映射场景
            String transferHttpPort = appStartConfig.get("server.port.transfer");
            String httpPort = appStartConfig.get("server.port");
            String httpHost = appStartConfig.get("server.host");

            log.info("app 启动参数 ：" + JsonUtil.toJson(appStartConfig));

            if (realPort == null) {
                if (CommonUtil.isNotEmpty(transferHttpPort)) {
                    realPort = transferHttpPort;
                }

            }
            if (realPort == null) {
                if (CommonUtil.isNotEmpty(httpPort)) {
                    realPort = httpPort;
                }
            }

            if (realPort == null) {
                realPort = SpringConfig.getConfig("server.port");
            }

            if (realPort == null) {
                log.error("没有获取到 http.port 端口");
                System.exit(0);
            }

            if (!realPort.matches("\\d+")) {
                log.error("http.port 非法，请检查启动参数, " + realPort);
                System.exit(0);
            }

            if (httpHost == null || !CommonUtil.isIpv4(httpHost)) {
                log.info("如果需要修改服务注册ip, 可以在启动添加启动参数 --server.host=x.x.x.x ");
                try {
                    List<String> ips = CommonUtil.getLocalIpAddress();
                    if (CommonUtil.isNotEmpty(ips)) {
                        httpHost = ips.get(0);
                    }
                    // InetAddress addr = InetAddress.getLocalHost();
                    // 获取本机ip
                    // httpHost = addr.getHostAddress().toString();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }

            if (httpHost == null) {
                log.warn("无法获取到本地ip");
            }

            currentIpPort = new String[]{httpHost, String.valueOf(realPort)};
        }

        return currentIpPort;
    }

    public static String getLocalServerIp() {
        Map<String, String> appStartConfig = getConfig();

        // 用于兼容 k8s docker 内部端口映射场景
        String httpHost = appStartConfig.get("server.host");

        if (httpHost == null || !CommonUtil.isIpv4(httpHost)) {
            try {
                List<String> ips = CommonUtil.getLocalIpAddress();
                if (CommonUtil.isNotEmpty(ips)) {
                    httpHost = ips.get(0);
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
        if (httpHost == null) {
            log.warn("无法获取到本地ip");
        }

        return httpHost;
    }

    private static Map<String, String> getConfig() {
        String cmd = System.getProperty("sun.java.command");

        String[] splits = cmd.split("\\s+");

        int index = cmd.indexOf("--server.host");

        Map<String, String> appStartConfig = new HashMap<String, String>();
        for (String string : splits) {

            if (string.matches("--.*?=.*?")) {

                String[] tmps = string.split("=");
                if (tmps.length != 2) {
                    continue;
                }
                String key = tmps[0].substring(2);
                String value = tmps[1].trim();

                appStartConfig.put(key, value);

            }
        }

        return appStartConfig;
    }

}
