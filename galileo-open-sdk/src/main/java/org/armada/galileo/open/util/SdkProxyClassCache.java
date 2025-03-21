package org.armada.galileo.open.util;

import lombok.extern.slf4j.Slf4j;
import org.armada.galileo.open.proxy.RunnableProxy;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author xiaobo
 * @date 2021/11/16 7:47 下午
 */
@Slf4j
public class SdkProxyClassCache {

    private static Map<String, Object> cache = new ConcurrentHashMap<>();

    private static Map<String, ThreadGroup> subThreadGroup = new ConcurrentHashMap<>();

    private static Map<String, RunnableProxy> mainRunnable = new ConcurrentHashMap<>();

    private static Map<String, Thread> mainThread = new ConcurrentHashMap<>();

    public static void startMainThread(String type, RunnableProxy runnable) {
        if (mainRunnable.get(type) != null) {
            try {
                mainRunnable.get(type).beforeDestroy();
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }

        if (mainThread.get(type) != null) {
            try {
                mainThread.get(type).stop();
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }

        mainRunnable.put(type, runnable);
        Thread t = new Thread(runnable);
        t.start();

        mainThread.put(type, t);
    }


    public static void stopSubThread(String type) {
        ThreadGroup tg = subThreadGroup.get(type);
        if (tg != null) {
            try {
                tg.stop();
            } catch (Exception e) {
            }
        }
    }

    /**
     * @param type     haiq_to_customer   customer_to_haiq
     * @param runnable
     */
    public static void startSubThread(String type, Runnable runnable) {

        ThreadGroup tg = subThreadGroup.get(type);
        if (tg == null) {
            synchronized (log) {
                tg = subThreadGroup.get(type);
                if (tg == null) {
                    tg = new ThreadGroup(type);
                    subThreadGroup.put(type, tg);
                }
            }
        }

        Thread t = new Thread(tg, runnable);
        t.start();
    }


    public static Object get(String key) {
        return cache.get(key);
    }

    public static void put(String key, Object obj) {
        Object old = cache.get(key);
        if (old != null) {
            // 回收对象
            old = null;
        }
        cache.put(key, obj);
    }

    public static void clear() {
        for (Iterator<String> it = cache.keySet().iterator(); it.hasNext(); ) {
            String key = it.next();
            Object obj = cache.get(key);
            // 回收对象
            obj = null;
            it.remove();
        }
    }

    public static enum ClassType {
        CustomerToHaiqGlobal,

        HaiqToCustomerGlobal,
    }


}
