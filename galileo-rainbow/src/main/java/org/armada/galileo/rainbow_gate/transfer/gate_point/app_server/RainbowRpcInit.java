package org.armada.galileo.rainbow_gate.transfer.gate_point.app_server;

import org.armada.galileo.common.lock.ConcurrentLock;
import org.armada.galileo.rainbow_gate.transfer.discovery.RainbowDiscoveryFacade;
import org.armada.galileo.rainbow_gate.transfer.gate_point.app_client.AsyncRequestCache;

/**
 * AppServer 端服务化接口注册类
 *
 * @author xiaobowang
 */
public class RainbowRpcInit {

    public static String appName = null;

    public static void init(String appName, String configConterUrl) {
        RainbowRpcInit.appName = appName;
        AsyncRequestCache.setupCachePath(appName);
        RainbowDiscoveryFacade.init(appName, configConterUrl);
    }

    public static void init(String appName, String configConterUrl, ConcurrentLock concurrentLock) {
        RainbowRpcInit.appName = appName;
        AsyncRequestCache.setupCachePath(appName);
        RainbowDiscoveryFacade.init(appName, configConterUrl);
        AppServer.setConcurrentLock(concurrentLock);
    }

}
