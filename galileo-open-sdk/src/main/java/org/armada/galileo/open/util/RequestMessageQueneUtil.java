package org.armada.galileo.open.util;

import lombok.extern.slf4j.Slf4j;
import org.armada.galileo.open.dal.entity.OpenRequestMessage;
import org.armada.galileo.open.service.OpenApiService;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author xiaobo
 * @date 2021/6/18 11:28 上午
 */
@Slf4j
public class RequestMessageQueneUtil {

    private static LinkedBlockingQueue<OpenRequestMessage> quene = new LinkedBlockingQueue<OpenRequestMessage>();

    private static Executor ex = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    private static OpenApiService openApiService;

    public static void setOpenApiService(OpenApiService openApiService) {
        RequestMessageQueneUtil.openApiService = openApiService;
    }

    /**
     * 添加一条消息
     *
     * @param data
     */
    public static void push(OpenRequestMessage data) {
        if (openApiService != null) {
            ex.execute(() -> {
                //quene.add(data);
                openApiService.insert(data);
            });
        }
    }


}
