package org.armada.galileo.open.cache;

import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.armada.galileo.common.lock.ConcurrentLock;
import org.armada.galileo.common.util.Pair;
import org.armada.galileo.es.mapper.EsBaseMapperImpl;
import org.armada.galileo.open.dal.entity.OpenAppConfig;
import org.armada.galileo.open.dal.entity.OpenInterfaceConfig;
import org.armada.galileo.open.proxy.HttpOpenApServlet;
import org.armada.galileo.open.service.OpenApiService;
import org.armada.galileo.open.util.RequestMessageQueneUtil;
import org.armada.galileo.open.web.rpc.OpenApiConfigRpc;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author xiaobo
 * @date 2023/2/7 10:45
 */
@Slf4j
public class OpenApiCacheUtil {


    public static void setup(OpenApiService openApiService) {
        OpenApiConfigRpc.setOpenApiService(openApiService);
        HttpOpenApServlet.setOpenApiService(openApiService);
        RequestMessageQueneUtil.setOpenApiService(openApiService);
//        OpenApiCacheUtil.concurrentLock = concurrentLock;
//        EsBaseMapperImpl.concurrentLock = concurrentLock;
    }


    public static void setup(ConcurrentLock concurrentLock) {
        OpenApiCacheUtil.concurrentLock = concurrentLock;
        // EsBaseMapperImpl.concurrentLock = concurrentLock;
    }


    // ----

    public static ConcurrentLock concurrentLock;

    // --------------------- appConfig  ---------------------

    /**
     * app 的密钥 （ appId：appSecret ）
     */
    private static Map<String, OpenAppCache> appIdCache = new HashMap<>();

    /**
     * 更新应用配置信息
     *
     * @param openAppConfig
     */
    public static void putOpenAppConfig(OpenAppConfig openAppConfig) {
        OpenAppCache cache = new OpenAppCache();
        cache.setAppId(openAppConfig.getAppId())
                .setAppSecret(openAppConfig.getAppSecret())
                .setApiUrls(openAppConfig.getApiUrls());

        synchronized (log) {
            appIdCache.put(openAppConfig.getAppId(), cache);
        }
    }

    /**
     * 删除应用配置
     *
     * @param appId
     */
    public static void removeOpenAppConfig(String appId) {
        synchronized (log) {
            appIdCache.remove(appId);
        }
    }

    /**
     * 获取应用配置缓存
     *
     * @param appId
     * @return
     */
    public static OpenAppCache getOpenAppCache(String appId) {
        return appIdCache.get(appId);
    }


    @Data
    @Accessors(chain = true)
    public static class OpenAppCache {
        private String appId;

        private String appSecret;

        private List<String> apiUrls;
    }

    // --------------------- transferConfig  ---------------------

    private static Object lock = new Object();

    /**
     * api_code, api_url
     */
    private static Map<Pair<String, String>, OpenInterfaceConfig> interfaceConfigCache = new HashMap<>();

    public static void putInterfaceConfig(String appId, String apiCode, OpenInterfaceConfig interfaceConfig) {
        synchronized (lock) {
            interfaceConfigCache.put(Pair.of(appId, apiCode), interfaceConfig);
        }
    }

    public static OpenInterfaceConfig getInterfaceConfig(String appId, String apiCode) {
        return interfaceConfigCache.get(Pair.of(appId, apiCode));
    }

    public static void removeInterfaceConfig(String appId, String apiCode) {
        interfaceConfigCache.remove(Pair.of(appId, apiCode));
    }


    // ---------------------  业务处理对象  ---------------------

    /**
     * 业务处理对象
     */
    private static Map<String, Pair<Class, Method>> uriTargetObjectCache = new HashMap<>();

    /**
     * 更新业务处理对象缓存对象
     *
     * @param apiCode
     * @param cls
     * @param method
     */
    public static void putApiMapping(String apiCode, Class cls, Method method) {
        if (uriTargetObjectCache.get(apiCode) != null) {
            System.err.print("不能定义重复的 code: " + apiCode);
            System.exit(0);
        }
        uriTargetObjectCache.put(apiCode, Pair.of(cls, method));
    }


    public static Pair<Class, Method> getInvokeMethod(String apiCode) {
        return uriTargetObjectCache.get(apiCode);

    }


}
