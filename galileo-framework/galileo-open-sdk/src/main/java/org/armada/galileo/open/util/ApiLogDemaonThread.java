package org.armada.galileo.open.util;

import lombok.extern.slf4j.Slf4j;
import org.armada.galileo.annotation.openapi.ApiRole;
import org.armada.galileo.common.lock.ConcurrentLock;
import org.armada.galileo.common.util.CommonUtil;
import org.armada.galileo.open.constant.RequestErrorType;
import org.armada.galileo.open.constant.RequestMessageStatus;
import org.armada.galileo.open.dal.entity.OpenRequestMessage;
import org.armada.galileo.open.proxy.HttpOpenApServlet;
import org.armada.galileo.open.proxy.HttpOpenApiCallbackProxy;
import org.armada.galileo.open.proxy.quene.HaiqToCustomerUtil;
import org.armada.galileo.open.service.OpenApiService;
import org.armada.galileo.open.vo.OpenRequestMessageQueryVO;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * @author xiaobo
 * @date 2021/11/18 4:43 下午
 */
@Slf4j
public class ApiLogDemaonThread extends Thread {

    private OpenApiService openApiService;

    private HttpOpenApiCallbackProxy httpOpenApiCallbackProxy;

    private HttpOpenApServlet servlet;

    private ConcurrentLock concurrentLock;

    private String appName;

    @Deprecated
    public ApiLogDemaonThread(OpenApiService OpenApiService, HttpOpenApServlet servlet) {
        this(OpenApiService, null, null, servlet);
    }


    public ApiLogDemaonThread(OpenApiService OpenApiService, ConcurrentLock concurrentLock, String appName, HttpOpenApServlet servlet) {
        this.openApiService = OpenApiService;
        this.httpOpenApiCallbackProxy = new HttpOpenApiCallbackProxy(OpenApiService);
        this.servlet = servlet;
        this.appName = appName;
        this.concurrentLock = concurrentLock;
        if (CommonUtil.isEmpty(this.appName)) {
            this.appName = "";
        }
    }

    private static AtomicBoolean hasInit = new AtomicBoolean(false);

    public void run() {

        // 防止被多次初始化
        if (!hasInit.compareAndSet(false, true)) {
            return;
        }

        // 异步执行线程, 处理异步请求
        new Thread("nova-sdk-async") {
            public void run() {

                boolean lock = true;
                if (concurrentLock != null) {
                    try {
                        lock = concurrentLock.lock(appName + "_customer_to_haiq_push_to_quene", null);
                    } catch (Exception e) {
                    }
                }

                if (lock) {
                    try {
                        // 系统重启后，加载未执行的异步请求
                        OpenRequestMessageQueryVO vo = new OpenRequestMessageQueryVO();

                        vo.setIsAsync("Y");
                        vo.setStatus("doing");
                        vo.setApiTo(ApiRole.KYB);
                        List<OpenRequestMessage> asyncJobList = null; //openApiService.queryRequestMsg(vo);
                        if (asyncJobList != null && asyncJobList.size() > 0) {
                            for (OpenRequestMessage msg : asyncJobList) {
                                AsyncExecutorUtil.push(new AsyncExecutorUtil.RequestTask(msg.getRequestId(), msg.getApiUrl(), msg.getRequestJson()));
                            }
                        }
                    } finally {
                        if (concurrentLock != null) {
                            try {
                                concurrentLock.unlock(appName + "_customer_to_haiq_push_to_quene");
                            } catch (Exception e) {
                            }
                        }
                    }
                }


                while (true) {

                    try {
                        Thread.sleep(50);
                    } catch (Exception e) {
                    }

                    Long l1 = System.currentTimeMillis();
                    Throwable ex = null;
                    String resultJson = null;
                    Integer errorCode = 0;
                    String errorMsg = null;

                    AsyncExecutorUtil.RequestTask task = AsyncExecutorUtil.take();
                    try {
                        // 执行目标方法
                        org.armada.galileo.common.util.Pair<Class, java.lang.reflect.Method> pair = org.armada.galileo.open.cache.OpenApiCacheUtil.getInvokeMethod(task.getApiUrl());
                        if (pair == null) {
                            throw new org.armada.galileo.exception.BizException("接口地址不存在");
                        }

                        servlet.executeRequest(pair, task.getApiUrl(), task.getJsonContent());

                    } catch (Throwable e) {

                        if (e instanceof InvocationTargetException) {
                            e = ((InvocationTargetException) e).getTargetException();
                        }

                        errorMsg = e.getMessage();
                        if (e instanceof ErrorCodeException) {
                            ErrorCodeException e1 = (ErrorCodeException) e;
                            errorMsg = e1.getErrorMessage();
                        }
                        if (errorMsg == null) {
                            errorMsg = "haiq 接口异常";
                        }
                        ex = e;
                        log.error(e.getMessage(), e);

                    } finally {

                        try {
                            OpenRequestMessage rm = openApiService.findById(task.getRequestId());

                            if (ex != null) {
                                rm.setStatus(RequestMessageStatus.FAIL);
                                // 错误类型
                                if (ex instanceof IOException) {
                                    rm.setErrorType(RequestErrorType.IO);
                                } else {
                                    rm.setErrorType(RequestErrorType.BIZ);
                                }
                            } else {
                                rm.setStatus(RequestMessageStatus.SUCCESS);
                            }

                            rm.setErrorMessage(errorMsg);
                            rm.setTimeCost((int) (System.currentTimeMillis() - l1));
                            rm.setUpdateTime(System.currentTimeMillis());

                            openApiService.update(rm);

                        } catch (Exception e) {
                            log.error(e.getMessage(), e);
                        }
                    }

                }
            }
        }.start();

//        // 从缓存队列中获取日志数据 ，并写入
//        new Thread("nova-sdk-log") {
//            public void run() {
//                while (true) {
//                    try {
//                        OpenRequestMessage msg = RequestMessageQueneUtil.take();
//                        openApiService.insert(msg);
//
//                        Thread.sleep(100);
//                    } catch (Exception e) {
//                        log.error(e.getMessage(), e);
//                    }
//                }
//            }
//        }.start();


        // 消息回传、失败自动重试
        new Thread("nova-sdk-retry") {
            public void run() {

                while (true) {

                    boolean lock = true;
                    if (concurrentLock != null) {
                        try {
                            lock = concurrentLock.lock(appName + "_haiq_to_customer_auto_retry", null);
                        } catch (Exception e) {
                        }
                    }

                    if (!lock) {
                        continue;
                    }
                    try {
                        // 之前的消息还没有消费完
                        if (!HaiqToCustomerUtil.messageEmpty()) {
                            continue;
                        }

                        Thread.sleep(openApiService.getRetrySleepTime());

                        // 查询回传失败的请求
                        OpenRequestMessageQueryVO vo = new OpenRequestMessageQueryVO();
                        vo.setIsAsync("Y");

                        vo.setMaxRetryTime(openApiService.getMaxRetryTime());
                        vo.setStatus("fail,doing");
                        vo.setApiFrom(ApiRole.KYB);
                        vo.setQueryForNotify(true);
                        // vo.setLastUpdateTime(new Date(System.currentTimeMillis() - OpenApiService.getRetrySleepTime()));

                        List<OpenRequestMessage> list = openApiService.queryOpenRequestMessage(vo);
                        if (list == null || list.size() == 0) {
                            continue;
                        }

                        // httpOpenApiCallbackProxy.retry(list, false, true);

                        // 分组
                        List<String> groups = list.stream().filter(e -> CommonUtil.isNotEmpty(e.getMsgGroup())).map(e -> e.getMsgGroup()).distinct().collect(Collectors.toList());

                        // 分组不为为空，将这些分组的未回传的消息全部查出
                        if (CommonUtil.isNotEmpty(groups)) {

                            // 查询数据库里其他的分组消息
                            //if (list.size() >= vo.getPageSize()) {

                            vo = new OpenRequestMessageQueryVO();
                            vo.setGroups(groups);
                            vo.setStatus("fail,doing");
                            vo.setApiFrom(ApiRole.KYB);

                            List<OpenRequestMessage> groupMessage = openApiService.queryOpenRequestMessage(vo);
                            Map<String, List<OpenRequestMessage>> groupMap = groupMessage.stream().collect(Collectors.groupingBy(e -> e.getMsgGroup()));

                            retry(groupMap, groups);

                            // }
                            // 所有未回传的消息都在本次的查询结果里
//                            else {
//                                Map<String, List<OpenRequestMessageQueryVO>> groupMap = list.stream().collect(Collectors.groupingBy(e -> e.getMsgGroup()));
//                                retry(groupMap, groups);
//                            }
                        }

                        // 不带分组信息的
                        list = list.stream().filter(e -> {

                            if (e.getUpdateTime() == null) {
                                return false;
                            }

                            if (CommonUtil.isNotEmpty(e.getMsgGroup())) {
                                return false;
                            }

                            if (System.currentTimeMillis() - e.getUpdateTime() < openApiService.getRetrySleepTime()) {
                                return false;
                            }

                            return true;

                        }).collect(Collectors.toList());

                        httpOpenApiCallbackProxy.retry(list, false, true);


                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                    } finally {
                        if (concurrentLock != null) {
                            try {
                                concurrentLock.unlock(appName + "_haiq_to_customer_auto_retry");
                            } catch (Exception e) {
                            }
                        }
                    }
                }
            }

        }.start();

    }

    private void retry(Map<String, List<OpenRequestMessage>> groupMap, List<String> groups) {

        List<Map.Entry<String, List<OpenRequestMessage>>> list = new ArrayList<>(groupMap.entrySet());

        // 按分组出现的顺序回传
        Collections.sort(list, new Comparator<Map.Entry<String, List<OpenRequestMessage>>>() {
            @Override
            public int compare(Map.Entry<String, List<OpenRequestMessage>> o1, Map.Entry<String, List<OpenRequestMessage>> o2) {
                return Integer.valueOf(groups.indexOf(o1.getKey())).compareTo(Integer.valueOf(groups.indexOf(o2.getKey())));
            }
        });

        for (Map.Entry<String, List<OpenRequestMessage>> entry : list) {
            List<OpenRequestMessage> openRequestMessageList = entry.getValue();
            // 从大到小排序
            Collections.sort(openRequestMessageList, new Comparator<OpenRequestMessage>() {
                @Override
                public int compare(OpenRequestMessage o1, OpenRequestMessage o2) {
                    int sort = o2.getMsgSort().compareTo(o1.getMsgSort());
                    if (sort != 0) {
                        return sort;
                    }

                    return o1.getHappenTime().compareTo(o2.getHappenTime());
                }
            });

            // 按组回传
            httpOpenApiCallbackProxy.retry(openRequestMessageList, true, true);
        }
    }


}
