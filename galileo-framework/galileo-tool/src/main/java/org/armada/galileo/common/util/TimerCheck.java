package org.armada.galileo.common.util;

import lombok.extern.slf4j.Slf4j;

/**
 * @author xiaobo
 * @date 2023/2/8 20:29
 */
@Slf4j
public class TimerCheck {

    private static ThreadLocal<Long> local = new ThreadLocal<>();

    public static void start() {
        local.set(System.currentTimeMillis());
    }

    public static void checkpoint(String name) {

        if (local.get() == null) {
            Long now = System.currentTimeMillis();
            log.info("[" + name + "] starts");
            local.set(now);
        } else {
            Long now = System.currentTimeMillis();
            log.info("[" + name + "] cost: " + (now - local.get()) + "ms");
            local.set(System.currentTimeMillis());
        }
    }
}
