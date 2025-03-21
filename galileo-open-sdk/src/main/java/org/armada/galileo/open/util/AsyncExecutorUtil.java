package org.armada.galileo.open.util;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 异常执行队列
 *
 * @author xiaobo
 * @date 2021/6/18 11:28 上午
 */
@Slf4j
public class AsyncExecutorUtil {

    private static LinkedBlockingQueue<RequestTask> quene = new LinkedBlockingQueue<RequestTask>();

    private static Executor ex = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    /**
     * 添加一条消息
     *
     * @param data
     */
    public static void push(RequestTask data) {
        ex.execute(() -> {
            quene.add(data);
        });
    }

    /**
     * 从队列中取出一个消息
     *
     * @return
     */
    public static RequestTask take() {
        try {
            return quene.take();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * 待调用的参数信息
     */
    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RequestTask {

        /**
         * 请求号
         */
        private String requestId;

        /**
         * api 接口
         */
        private String apiUrl;

        /**
         * 请求参数
         */
        private String jsonContent;

    }

}
