package org.armada.galileo.open.proxy.quene;

import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.armada.galileo.open.dal.entity.OpenRequestMessage;

import java.util.Date;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

/**
 * socket ftp db 对接方式需使用该回传队列来进行回传
 *
 * @author xiaobo
 * @date 2022/1/10 3:03 下午
 */
@Slf4j
public class HaiqToCustomerUtil {

    private static LinkedBlockingQueue<HaiqToCustomerNotifyMessageGroup> autoQuene = new LinkedBlockingQueue<HaiqToCustomerNotifyMessageGroup>();

    private static LinkedBlockingQueue<HaiqToCustomerNotifyMessageGroup> manualQuene = new LinkedBlockingQueue<HaiqToCustomerNotifyMessageGroup>();

    private static Executor ex = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());


    /**
     * 获取队列里所有消息
     *
     * @return
     */
    public static HaiqToCustomerNotifyMessageGroup[] getAll() {
        HaiqToCustomerNotifyMessageGroup[] arr = new HaiqToCustomerNotifyMessageGroup[autoQuene.size()];
        arr = autoQuene.toArray(arr);
        return arr;
    }


    /**
     * 清空队列
     *
     * @return
     */
    public static void clearAll() {
        autoQuene.clear();
    }

    /**
     * 缓存一个待回传的消息至缓存队列
     *
     * @param msgList
     */
    public static void push(List<OpenRequestMessage> msgList, boolean groupMsg, Boolean autoNotify) {

        List<HaiqToCustomerNotifyMessage> messageList = msgList.stream().map(e -> {
            HaiqToCustomerNotifyMessage msg = new HaiqToCustomerNotifyMessage();
            msg.setRequestId(e.getRequestId());
            msg.setApiUrl(e.getApiUrl());
            msg.setNotifyJson(e.getRequestJson());
            msg.setLastUpdateTime(e.getUpdateTime());
            msg.setMsgGroup(e.getMsgGroup());
            msg.setMsgSort(e.getMsgSort());
            msg.setAutoNotify(autoNotify);

            return msg;
        }).collect(Collectors.toList());

        HaiqToCustomerNotifyMessageGroup group = new HaiqToCustomerNotifyMessageGroup();
        group.setGroupMsg(groupMsg);
        group.setMessages(messageList);


        if(autoNotify){
            autoQuene.add(group);
        }
        else{
            manualQuene.add(group);
        }

    }

    /**
     * 从列队中获取一个待回传的任务
     *
     * @return
     */
    public static HaiqToCustomerNotifyMessageGroup poll() {
        try {
            return autoQuene.poll();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * 从列队中获取一个待回传的任务
     *
     * @return
     */
    public static HaiqToCustomerNotifyMessageGroup pollManual() {
        try {
            return manualQuene.poll();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    public static boolean messageEmpty() {
        return autoQuene.isEmpty();
    }


    @Data
    @Accessors(chain = true)
    public static class HaiqToCustomerNotifyMessage {

        private String requestId;

        private String msgGroup;

        private Integer msgSort;

        private Long lastUpdateTime;

        private String apiUrl;

        private String notifyJson;

        /**
         * 自动机制触发的回传
         */
        private Boolean autoNotify;


    }

    @Data
    @Accessors(chain = true)
    public static class HaiqToCustomerNotifyMessageGroup {

        /**
         * 是否为分组消息， 如果为是，则第任何一条消息回传失败都需要终断本次所有回传
         */
        private boolean groupMsg;

        private String groupValue;

        private List<HaiqToCustomerNotifyMessage> messages;
    }

}
