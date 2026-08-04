package org.armada.galileo.mybatis.sequence;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.armada.galileo.common.util.CommonUtil;
import org.armada.galileo.mybatis.util.TransactionUtil;
import org.springframework.transaction.TransactionDefinition;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 统一算号器, 用于生成连续编号
 *
 * @author xiaobo
 * @date 2022/12/18 18:07
 */
@Slf4j
@Setter
public class SequenceNoService {

    private String tableName;

    private DbMapper<SequenceNo> mapper;

    private TransactionUtil transactionUtil;

    /**
     * 以id为单位的局部锁
     */
    private static Map<String, AtomicBoolean> threadWaiting = new ConcurrentHashMap<>();

    private static Map<String, String> initMap = new ConcurrentHashMap<>();

    private static final String Waiting_Singal = "Waiting_Singal";

    private static final String Release_Singal = "Release_Singal";

    private static final String Common = "common";

    private SequenceNoService() {
    }

    public static SequenceNoService instance(String tableName, DbMapper<SequenceNo> mapper, TransactionUtil transactionUtil) {
        SequenceNoService service = new SequenceNoService();
        service.tableName = tableName;
        service.mapper = mapper;
        service.transactionUtil = transactionUtil;
        return service;
    }

    /**
     * 编号生成器，返回: 头部 + 日期 + 分隔符 + 序号，示例: input("ABC", 3), output:  ABC20220101-001
     *
     * @param head 编号头
     * @param len  尾部序号部分的长度
     * @return
     */
    public String generateWithHeadDateSplitSeq(String head, int len) {
        String day = CommonUtil.format(new Date(), "yyyyMMdd");
        return generate(head, day, len, true, true);
    }

    /**
     * 编号生成器，返回: 头部 + 日期 + 序号，示例: input("ABC", 3), output:  ABC20220101001
     *
     * @param head 编号头
     * @param len  尾部序号部分的长度
     * @return
     */
    public String generateWithHeadDateSeq(String head, int len) {
        String day = CommonUtil.format(new Date(), "yyyyMMdd");
        return generate(head, day, len, false, true);
    }

    /**
     * 编号生成器，返回: 日期 + 序号，示例: input("ABC", 3), output: 20240103001
     *
     * @param head 编号头
     * @param len  尾部序号部分的长度
     * @return
     */
    public String generateWithDateSeq(String head, int len) {
        String day = CommonUtil.format(new Date(), "yyyyMMdd");
        String no = generate(head, day, len, false, true);
        return no.substring(head.length());
    }

    /**
     * 编号生成器，返回: 头部 + 序号， 示例: input("ABC", 3), output: ABC001
     *
     * @param head 编号头
     * @param len  尾部序号部分的长度
     * @return
     */
    public String generateWithHeadSeq(String head, int len) {
        return generate(head, null, len, false, true);
    }


    /**
     * 编号生成器，返回: 序号， 示例: input("ABC", 3), output: 001
     *
     * @param head 编号头
     * @param len  尾部序号部分的长度
     * @return
     */
    public String generateOnlySeq(String head, int len) {
        String no = generate(head, null, len, false, true);
        return no.substring(head.length());
    }


    /**
     * @param head        头部
     * @param day         是否指定日期
     * @param len         序号的长度
     * @param isWithSplit 是否以序号前加上-
     * @return
     */
    private String generate(String head, String day, int len, boolean isWithSplit, boolean appendHead) {

        StringBuilder stringBuilder = new StringBuilder();
        if (appendHead) {
            stringBuilder.append(head);
        }
        if (day != null) {
            stringBuilder.append(day);

            if (isWithSplit) {
                stringBuilder.append("-");
            }
        }

        String id = stringBuilder.toString();

        while (true) {
            try {
                String no = transactionUtil.doTransaction(() -> {

                    SequenceNo sequenceNo = mapper.selectById4Update(tableName, id);
                    if (sequenceNo == null) {

                        AtomicBoolean lock = threadWaiting.get(id);

                        if (lock == null) {
                            lock = threadWaiting.get(id);
                            synchronized (log) {
                                if (lock == null) {
                                    lock = new AtomicBoolean(false);
                                    threadWaiting.put(id, lock);
                                }
                            }
                        }

                        if (lock.compareAndSet(false, true)) {
                            // System.out.println("need add: " + id + "  " + Thread.currentThread().getName());
                            try {
                                SequenceNo record = new SequenceNo();
                                record.setId(id);
                                record.setHead(head);
                                record.setDay(day != null ? day : Common);
                                record.setCurrentIndex(0);
                                record.setCurrentNo("#");
                                mapper.insertRecord(tableName, record);
                            } finally {
                                // 唤醒信号
                                return Release_Singal;
                            }
                        } else {
                            // 等待信号
                            return Waiting_Singal;
                        }
                    }

                    Integer currentIndex = sequenceNo.getCurrentIndex() + 1;
                    String newNo = currentIndex.toString();
                    StringBuilder sb = new StringBuilder(id);
                    if (newNo.length() < len) {
                        for (int i = 0; i < len - newNo.length(); i++) {
                            sb.append("0");
                        }
                    }
                    sb.append(newNo);

                    String lastNo = sb.toString();

                    SequenceNo update = new SequenceNo();
                    update.setId(id);
                    update.setCurrentIndex(currentIndex);
                    update.setCurrentNo(lastNo);

                    mapper.updateRecord(tableName, update);

                    return lastNo;

                }, TransactionDefinition.PROPAGATION_REQUIRES_NEW);

                if (no == null) {
                    continue;
                }
                // 唤醒
                else if (Release_Singal.equals(no)) {
                    AtomicBoolean lock = threadWaiting.get(id);
                    if (lock == null) {
                        continue;
                    }
                    try {
                        synchronized (lock) {
                            // System.out.println("lock.notifyAll();");
                            lock.notifyAll();
                        }
                    } finally {
                        threadWaiting.remove(id);
                        continue;
                    }
                }
                // 等待
                else if (Waiting_Singal.equals(no)) {
                    AtomicBoolean lock = threadWaiting.get(id);
                    if (lock == null) {
                        continue;
                    }
                    try {
                        synchronized (lock) {
                            // long l1 = System.currentTimeMillis();
                            // System.out.println("waiting lock");
                            lock.wait();
                            // long l2 = System.currentTimeMillis();
                            // System.out.println("has been notified, " + (l2 - l1) + "ms");
                        }
                    } finally {
                        continue;
                    }
                }

                return no;

            } catch (Exception e) {
                if (isSequenceTableNotExist(e)) {
                    log.error("表 {} 不存在，退出算号循环: {}", tableName, e.getMessage());
                    if (e instanceof RuntimeException) {
                        throw (RuntimeException) e;
                    }
                    throw new RuntimeException(e);
                }
                log.error(e.getMessage());
                continue;
            }

        }

    }

    /**
     * 判断异常是否为 sequence_no 表不存在，例如：
     * Table 'scm_tms.tms_sequence_no' doesn't exist
     */
    private boolean isSequenceTableNotExist(Throwable e) {
        Throwable t = e;
        while (t != null) {
            String msg = t.getMessage();
            if (msg != null) {
                String lower = msg.toLowerCase();
                boolean tableMissing = lower.contains("doesn't exist") || lower.contains("does not exist");
                if (tableMissing && (msg.contains(tableName) || lower.contains("sequence_no"))) {
                    return true;
                }
            }
            t = t.getCause();
        }
        return false;
    }


    // ---------- SCM 兼容别名 ----------

    /** @deprecated 使用 {@link #generateWithHeadDateSplitSeq(String, int)} */
    @Deprecated
    public String generateSequenceNoWithSplit(String head, int len) {
        return generateWithHeadDateSplitSeq(head, len);
    }

    /** @deprecated 使用 {@link #generateWithHeadDateSplitSeq(String, int)} */
    @Deprecated
    public String generateSequenceNoWithSplit(String head, int len, String dateFormat) {
        String day = CommonUtil.format(new Date(), dateFormat);
        return generate(head, day, len, true, true);
    }

    /** @deprecated 使用 {@link #generateWithHeadDateSeq(String, int)} */
    @Deprecated
    public String generateSequenceNo(String head, int len) {
        return generateWithHeadDateSeq(head, len);
    }

    /** @deprecated 使用 {@link #generateWithDateSeq(String, int)} */
    @Deprecated
    public String generateSequenceNoWithOutHead(String head, int len) {
        return generateWithDateSeq(head, len);
    }

    /** @deprecated 使用 {@link #generateWithHeadSeq(String, int)} */
    @Deprecated
    public String generateSequenceNoSimple(String head, int len) {
        return generateWithHeadSeq(head, len);
    }

}
