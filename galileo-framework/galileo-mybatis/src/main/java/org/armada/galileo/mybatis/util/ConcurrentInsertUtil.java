package org.armada.galileo.mybatis.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.transaction.TransactionDefinition;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 数据库并发写入工具类， 适用于有唯一性索引并发写入数据库的场景
 *
 * @author xiaobo
 * @date 2023/3/2 17:25
 */

public interface ConcurrentInsertUtil<Entity, ID extends Serializable> {

    static final int Waiting_Singal = 1;

    static final int Release_Singal = 2;

    static final int Success_Singal = 3;

    static Map<Serializable, AtomicBoolean> threadWaiting = new ConcurrentHashMap<>();

    static Logger log = LoggerFactory.getLogger(ConcurrentInsertUtil.class);

    public TransactionUtil getTransactionUtil();

    /**
     * 加锁的方法
     *
     * @param id
     * @return
     */
    public Entity selectWithLock(ID id);

    /**
     * 新增的方法
     *
     * @param t
     */
    public void doInsert(Entity t);

    public static interface Updater<Entity> {
        /**
         * 获取到锁之后的更新操作
         *
         * @param t
         */
        public void doUpdate(Entity t);

        /**
         * 如果数据库不存在，需要初始化一个对象到数据库， 这里返回这个初始化对象<br/>
         * 这里只需定义对象即可， 只有根据主键去 select for update 为空时，才会调用这个方法
         *
         * @return
         */
        public Entity getInitEntity();
    }

    /**
     * 使用该方法中， 方法外无需再套一层事务
     *
     * @param id
     * @param updater
     */
    public default void doUpdate(ID id, Updater<Entity> updater) {

        while (true) {

            try {
                int sign = getTransactionUtil().doTransaction(() -> {

                    Entity record = selectWithLock(id);

                    // 数据库记录不存在（没有获取到锁）
                    if (record == null) {
                        // 全局锁
                        AtomicBoolean lock = threadWaiting.get(id);
                        if (lock == null) {
                            synchronized (log) {
                                lock = threadWaiting.get(id);
                                if (lock == null) {
                                    lock = new AtomicBoolean(false);
                                    threadWaiting.put(id, lock);
                                }
                            }
                        }

                        // 加锁成功
                        if (lock.compareAndSet(false, true)) {

                            //需要加锁的记录不存在， 则新增
                            try {
                                doInsert(updater.getInitEntity());
                            }
                            // 唯一索引重复，说明该记录已经存在
                            catch (DuplicateKeyException e) {
                                return Release_Singal;
                            }
                            // 其他类型的错误需要终止
                            catch (Exception e) {
                                try {
                                    synchronized (lock) {
                                        lock.notifyAll();
                                    }
                                } finally {
                                    threadWaiting.remove(id);
                                }
                                throw e;
                            }
                            // 唤醒信号
                            return Release_Singal;

                        }
                        // 加锁失败
                        else {
                            // 等待信号
                            return Waiting_Singal;
                        }
                    }

                    updater.doUpdate(record);

                    return Success_Singal;

                }, TransactionDefinition.PROPAGATION_REQUIRES_NEW);

                // 唤醒
                if (Release_Singal == sign) {
                    AtomicBoolean lock = threadWaiting.get(id);
                    if (lock == null) {
                        continue;
                    }
                    try {
                        synchronized (lock) {
                            lock.notifyAll();
                        }
                    } finally {
                        threadWaiting.remove(id);
                        continue;
                    }
                }
                // 等待
                else if (Waiting_Singal == sign) {
                    AtomicBoolean lock = threadWaiting.get(id);
                    if (lock == null) {
                        continue;
                    }
                    try {
                        synchronized (lock) {
                            lock.wait();
                        }
                    } catch (InterruptedException e) {
                    } finally {
                        continue;
                    }
                }
                // 成功
                else {
                    break;
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                break;
            }

        }
    }


}
