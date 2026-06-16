package org.armada.galileo.mvc_plus.util;

import org.apache.logging.log4j.ThreadContext;

/**
 * @author xiaobo
 * @date 2023/6/9 11:40
 */
public class TraceIdUtil {


    private static final String TraceId = "TRACE_ID";

    /**
     * 设置日志链路追踪id
     *
     * @param traceId
     */
    public static void putTraceId(String traceId) {
        ThreadContext.put(TraceId, traceId);
    }

    /**
     * 获取日志链路追踪id
     *
     * @return
     */
    public static String getTraceId() {
        return ThreadContext.get(TraceId);
    }

    /**
     * 清除日志链路追踪id
     */
    public static void remove() {
        ThreadContext.remove(TraceId);
    }

}
