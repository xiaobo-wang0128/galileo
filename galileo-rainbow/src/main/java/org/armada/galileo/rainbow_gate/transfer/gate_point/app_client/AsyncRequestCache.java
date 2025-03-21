package org.armada.galileo.rainbow_gate.transfer.gate_point.app_client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.armada.galileo.common.util.*;
import org.armada.galileo.rainbow_gate.transfer.connection.http.HttpPostUtil;
import org.armada.galileo.rainbow_gate.transfer.domain.protocol.AppRequestDomain;
import org.armada.galileo.rainbow_gate.transfer.domain.protocol.AppResponseDomain;
import org.armada.galileo.rainbow_gate.transfer.gate_codec.ByteCodec;
import org.armada.galileo.rainbow_gate.transfer.gate_codec.GateCodecUtil;
import org.armada.galileo.rainbow_gate.transfer.gate_point.register.RegisterUtil;
import org.armada.galileo.rainbow_gate.transfer.util.RainbowException;
import org.armada.galileo.rainbow_gate.transfer.util.RainbowUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author xiaobo
 * @date 2023/2/14 10:15
 */
@Slf4j
public class AsyncRequestCache {

    private static String cachPath = null;

    private static SnowSequence snowSequence = new SnowSequence();

    private static int processes = Runtime.getRuntime().availableProcessors();

    private static Executor ex = Executors.newFixedThreadPool(processes);

    /**
     * 最大并发请求数
     */
    private static final int MaxConcurrentNum = processes * 3;

    /**
     * 最大错误重试次数
     */
    private static final int MaxRetryTimes = Integer.MAX_VALUE;

    /**
     * 错误重试最大间隔时间
     */
    private static int MaxRetrySleepTime = 30000;

    /**
     * 磁盘扫描间隔时间
     */
    private static int sleepTime = 400;

    /**
     * 当前正在异步请求中的数量
     */
    private static AtomicInteger concurrentNum = new AtomicInteger(0);

    /**
     * 正在处理中的文件
     */
    private static Map<String, Byte> doingCahce = new ConcurrentHashMap<>();

    static final Byte EXIST = new Byte("1");


    public static void setupCachePath(String appName) {

        cachPath = System.getProperty("user.home");
        if (cachPath.endsWith(File.separator)) {
            cachPath = cachPath.substring(0, cachPath.length() - 1);
        }
        cachPath += File.separator + "rainbow_async" + File.separator + appName + File.separator;

        File folder = new File(cachPath);
        if (!folder.exists()) {
            folder.mkdirs();
        }

        startListen();
    }


    static final int maxLen = 20;

    /**
     * @param apiCode
     * @param groupValue
     * @param id
     * @param times      第几次调用
     * @return
     */
    private static String generateTmpFilePath(String apiCode, String groupValue, String id, int times) {

        // 长度补位
        if (id.length() < maxLen) {
            StringBuffer tmp = new StringBuffer(id);
            for (int i = 0; i < maxLen - id.length(); i++) {
                tmp.append("0");
            }
            id = tmp.toString();
        }

        // group_apiCode___groupValue_id#timestamp#1
        if (groupValue != null) {
            return new StringBuilder(cachPath).append(File.separator)//
                    .append("group").append("_") //
                    .append(apiCode).append("___").append(groupValue).append("_") //
                    .append(id).append("#").append(System.currentTimeMillis())//
                    .append("#").append(times).toString();
        }
        // common_apiCode_id#timestamp#1
        else {
            return new StringBuilder(cachPath).append(File.separator)//
                    .append("common").append("_")//
                    .append(apiCode).append("_") //
                    .append(id).append("#").append(System.currentTimeMillis())//
                    .append("#").append(times).toString();//
        }

    }

    public static void main(String[] args) throws Exception {
        RequestInfo requestInfo = readRequestInfoFromFileName("common_doAsyncTest_1627571010125459458#1676878147708#1");

        System.out.println(JsonUtil.toJsonPretty(requestInfo));

        String path = "/Users/wangxiaobo/rainbow_async/oms-api";

        for (File file : new File(path).listFiles()) {
            System.out.println(file.getName());

            byte[] fileData = CommonUtil.readFileFromLocal(file);
            ByteCodec.Reader reader = new ByteCodec.Reader(fileData);

            String routeKey = reader.readString();
            String className = reader.readString();
            byte[] data = reader.readBytes();


            AppRequestDomain requestDomain = GateCodecUtil.decodeRequest(data);

            for (Object paramInput : requestDomain.getParamInputs()) {
                System.out.println(JsonUtil.toJsonPretty(paramInput));
            }
            System.out.println();
        }

    }

    private static RequestInfo readRequestInfoFromFileName(String fileName) {

        RequestInfo ri = new RequestInfo();

        String apiCode = null;
        String groupValue = null;
        String id = null;
        int times = 1;
        String group = null;
        long happenTime = -1L;

        int lastIndex = fileName.lastIndexOf("_");
        int end1 = fileName.indexOf("#");
        int end2 = fileName.lastIndexOf("#");

        if (end1 == -1 || end2 == -1) {
            return null;
        }
        if (end1 >= end2) {
            return null;
        }
        if (end1 <= lastIndex) {
            return null;
        }
        // group Message
        if (fileName.startsWith("group_")) {
            if (lastIndex == -1 || lastIndex <= 6) {
                return null;
            }
            group = fileName.substring(6, lastIndex);

            int tmpIndex = group.indexOf("___");
            if (tmpIndex == -1) {
                return null;
            }

            apiCode = group.substring(0, tmpIndex);
            groupValue = group.substring(tmpIndex + 3);
        }
        // common
        else {
            apiCode = fileName.substring(7, lastIndex);
        }

        id = fileName.substring(lastIndex + 1, end1);
        times = Convert.asInt(fileName.substring(end2 + 1));
        happenTime = Convert.asLong(fileName.substring(end1 + 1, end2));

        ri.setId(id);
        ri.setTimes(times);
        ri.setHappenTime(happenTime);
        ri.setApiCode(apiCode);
        ri.setGroupValue(groupValue);
        ri.setGroup(group);

        return ri;
    }


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    private static class RequestInfo {

        /**
         * 接口code
         */
        private String apiCode;

        /**
         * 分组值
         */
        private String groupValue;

        /**
         * 分组值， 包含 api_code
         */
        private String group;

        /**
         * snow id
         */
        private String id = null;
        /**
         * 执行次数
         */
        private int times = 1;
        /**
         * 发生时间
         */
        long happenTime = -1L;

        public boolean isGroup() {
            if (CommonUtil.isNotEmpty(group)) {
                return true;
            }
            return false;
        }

    }


    public static void push(AppRequestDomain domain, String apiCode) {

        byte[] data = GateCodecUtil.encodeRequest(domain);

        Long id = snowSequence.nextId();

        FileOutputStream fos = null;
        try {

            String groupValue = domain.getGroupValue();

            String filePath = generateTmpFilePath(apiCode, groupValue, id.toString(), 1);

            fos = new FileOutputStream(filePath);

            TmpFileUtil.writeString(fos, domain.getRouteKey());
            TmpFileUtil.writeString(fos, domain.getClassName());
            TmpFileUtil.writeBytes(fos, data);
            TmpFileUtil.writeString(fos, groupValue);

            fos.flush();

            fos.close();

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

    }


    // 异步调用执行器
    static class AsyncJob implements Runnable {

        private List<Pair<File, RequestInfo>> files;

        public AsyncJob(List<Pair<File, RequestInfo>> files) {
            this.files = files;
        }

        @Override
        public void run() {

            int successIndex = 0;

            for (Pair<File, RequestInfo> pair : files) {

                File file = pair.getLeft();
                RequestInfo requestInfo = pair.getRight();

                String fileName = file.getName();
                try {

                    log.info("start exec file: " + fileName);

                    if (!file.exists()) {

                        // log.info("start exec file, but file not exist: " + fileName);

                        doingCahce.remove(fileName);
                        concurrentNum.decrementAndGet();
                        continue;
                    }

                    byte[] fileData = CommonUtil.readFileFromLocal(file);
                    ByteCodec.Reader reader = new ByteCodec.Reader(fileData);

                    String routeKey = reader.readString();
                    String className = reader.readString();
                    byte[] data = reader.readBytes();

                    String appServerAddress = AppClient.getDirectRemoteAddress(className);
                    if (appServerAddress == null) {
                        appServerAddress = AppClient.getTargetRequestUrl(routeKey, className);
                    }

                    byte[] result = HttpPostUtil.request(appServerAddress, null, data);

                    AppResponseDomain actionResult = GateCodecUtil.decodeResponse(result);
                    if (actionResult.getCode() != 0) {
                        String tmp = appServerAddress;
                        if (tmp.endsWith(RegisterUtil.AppServerAddress)) {
                            tmp = tmp.substring(0, tmp.length() - RegisterUtil.AppServerAddress.length());
                        }
                        log.error("[app-client] rainbow-rpc response error，remote address:{}, remote error:{} ", tmp, actionResult.getMessage());

                        throw new RainbowException(actionResult.getMessage());
                    }
                    // 调用成功删除文件
                    file.delete();

                    successIndex++;

                    if (!requestInfo.isGroup()) {
                        doingCahce.remove(fileName);
                        concurrentNum.decrementAndGet();
                    }

                } catch (Exception e) {

                    // 已重试的次数
                    int errorTime = requestInfo.getTimes();

                    log.error("[async] 异步调用失败, fileId:{}, 当前重试次数:{}, error:{} ", fileName, errorTime, e.getMessage());
                    log.error(e.getMessage(), e);

                    // 分组消息需要直接退出当前线程
                    if (requestInfo.isGroup()) {
                        break;
                    }
                    //  普通消息处理
                    else {
                        // 超过最大重试次数
                        if (errorTime >= MaxRetryTimes) {
                            file.delete();
                            log.error("[async] 异步通知超过最大重试次数, 删除文件, fileId:{}", fileName);
                        }
                        // 更改文件名，下一次重试
                        else {
                            String errorFilePath = generateTmpFilePath(requestInfo.getApiCode(), requestInfo.getGroupValue(), requestInfo.getId(), errorTime + 1);
                            File renameFile = new File(errorFilePath);
                            file.renameTo(renameFile);
                        }

                        doingCahce.remove(fileName);
                        concurrentNum.decrementAndGet();

                    }
                }
            }

            RequestInfo requestInfo = files.get(0).getRight();

            if (requestInfo.isGroup()) {

                // 中途有失败的消息
                if (successIndex < files.size()) {

                    int errorTime = requestInfo.getTimes();

                    for (int i = successIndex; i < files.size(); i++) {
                        Pair<File, RequestInfo> tmpPair = files.get(i);
                        String tmpFileName = tmpPair.getLeft().getName();

                        // 超过最大重试次数
                        if (errorTime >= MaxRetryTimes) {
                            log.error("[async] 异步通知超过最大重试次数, 删除文件 fileId:{}", tmpFileName);
                            tmpPair.getLeft().delete();
                        }
                        // 更改文件名，下一次重试
                        else {
                            String errorFilePath = generateTmpFilePath(tmpPair.getRight().getApiCode(), tmpPair.getRight().getGroupValue(), tmpPair.getRight().getId(), errorTime + 1);
                            File renameFile = new File(errorFilePath);
                            tmpPair.getLeft().renameTo(renameFile);
                        }
                    }
                }

                doingCahce.remove(requestInfo.getGroup());
                concurrentNum.addAndGet(-files.size());
            }

        }
    }


    private static void startListen() {

        new Thread(() -> {
            while (true) {
                try {

                    File folder = new File(cachPath);

                    File[] files = folder.listFiles();

                    if (files == null || files.length == 0) {
                        Thread.sleep(sleepTime);
                        continue;
                    }

                    if (concurrentNum.get() >= MaxConcurrentNum) {
                        log.info("[async-notify] 并发回传请求数已超限制, maxNum:{}", MaxConcurrentNum);
                        Thread.sleep(sleepTime);
                        continue;
                    }

                    List<File> fileList = CommonUtil.asList(files);

                    // 排序
                    Collections.sort(fileList, new Comparator<File>() {
                        @Override
                        public int compare(File o1, File o2) {
                            return o1.getName().compareTo(o2.getName());
                        }
                    });

                    Map<String, List<Pair<File, RequestInfo>>> groupMessage = new HashMap<>();

                    List<Pair<File, RequestInfo>> commonMessage = new ArrayList<>();

                    List<Pair<File, RequestInfo>> retryMessage = new ArrayList<>();

                    for (File file : fileList) {

                        String fileName = file.getName();

                        RequestInfo requestInfo = readRequestInfoFromFileName(fileName);

                        if (requestInfo == null) {
                            file.delete();
                            continue;
                        }

                        // 分组消息
                        if (requestInfo.isGroup()) {

                            if (doingCahce.get(requestInfo.getGroup()) != null) {
                                continue;
                            }

                            List<Pair<File, RequestInfo>> messageList = groupMessage.get(requestInfo.getGroup());
                            if (messageList == null) {
                                messageList = new ArrayList<>();
                                groupMessage.put(requestInfo.getGroup(), messageList);
                            }
                            messageList.add(Pair.of(file, requestInfo));

                        }
                        // 普通
                        else {

                            if (doingCahce.get(fileName) != null) {
                                continue;
                            }

                            if (requestInfo.getTimes() <= 1) {
                                commonMessage.add(Pair.of(file, requestInfo));
                            }
                            // 次数大于 1
                            else {
                                long errorTimeStamp = requestInfo.getHappenTime();
                                if (System.currentTimeMillis() - errorTimeStamp < MaxRetrySleepTime) {
                                    continue;
                                }
                                retryMessage.add(Pair.of(file, requestInfo));
                            }
                        }
                    }


                    // 优先执行普通异步消息
                    if (commonMessage.size() > 0) {

                        for (List<Pair<File, RequestInfo>> tmpFileList : CommonUtil.split(commonMessage, processes)) {

                            ex.execute(new AsyncJob(tmpFileList));

                            for (Pair<File, RequestInfo> tmpPair : tmpFileList) {
                                doingCahce.put(tmpPair.getLeft().getName(), EXIST);
                            }
                            concurrentNum.addAndGet(tmpFileList.size());

                            if (concurrentNum.get() >= MaxConcurrentNum) {
                                break;
                            }
                        }
                    }

                    if (concurrentNum.get() < MaxConcurrentNum) {

                        // 执行普通的分组消息
                        if (groupMessage.size() > 0) {
                            for (Map.Entry<String, List<Pair<File, RequestInfo>>> entry : groupMessage.entrySet()) {
                                // 分组消息中有1条未达到重试时间，立即退出本次调用
                                boolean needExecuteGroup = true;
                                for (Pair<File, RequestInfo> p : entry.getValue()) {
                                    if (p.getRight().getTimes() <= 1) {
                                        break;
                                    }
                                    long errorTimeStamp = p.getRight().getHappenTime();
                                    if (System.currentTimeMillis() - errorTimeStamp < MaxRetrySleepTime) {
                                        needExecuteGroup = false;
                                        break;
                                    }
                                }
                                if (needExecuteGroup) {
                                    ex.execute(new AsyncJob(entry.getValue()));
                                    doingCahce.put(entry.getValue().get(0).getRight().getGroup(), EXIST);
                                    concurrentNum.addAndGet(entry.getValue().size());
                                }
                                if (concurrentNum.get() >= MaxConcurrentNum) {
                                    break;
                                }
                            }
                        }

                        // 最后执行出错的信息
                        if (concurrentNum.get() < MaxConcurrentNum && retryMessage.size() > 0) {
                            int maxLen = MaxConcurrentNum - concurrentNum.get();
                            if (maxLen > 0) {
                                if (retryMessage.size() > maxLen) {
                                    retryMessage = retryMessage.subList(0, maxLen);
                                }
                                ex.execute(new AsyncJob(retryMessage));
                                for (Pair<File, RequestInfo> tmpPair : retryMessage) {
                                    doingCahce.put(tmpPair.getLeft().getName(), EXIST);
                                }
                                concurrentNum.addAndGet(retryMessage.size());
                            }
                        }
                    }

                    Thread.sleep(sleepTime);

                } catch (InterruptedException e) {

                    break;

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    try {
                        Thread.sleep(sleepTime);
                    } catch (Exception exx) {
                    }
                }
            }

        }, "rainbow-async").start();

    }


    private static byte[] ZERO = RainbowUtil.intToByteArray(0);

    public static class TmpFileUtil {

        public static void writeInteger(OutputStream bos, Integer value) throws Exception {
            byte[] buf = RainbowUtil.intToByteArray(value);
            bos.write(buf);
        }

        public static void writeString(OutputStream bos, String value) throws Exception {
            if (value == null || value.matches("\\s*")) {
                bos.write(ZERO);
                return;
            }
            byte[] bufs = value.getBytes("utf-8");
            writeInteger(bos, bufs.length);
            bos.write(bufs);
        }

        public static void writeBytes(OutputStream bos, byte[] bufs) throws Exception {
            if (bufs == null || bufs.length == 0) {
                bos.write(ZERO);
                return;
            }
            writeInteger(bos, bufs.length);
            bos.write(bufs);
        }
    }


}
