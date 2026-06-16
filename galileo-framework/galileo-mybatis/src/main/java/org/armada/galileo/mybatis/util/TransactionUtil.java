package org.armada.galileo.mybatis.util;


import org.armada.galileo.exception.BizException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.concurrent.Callable;

public class TransactionUtil {

    private PlatformTransactionManager dataSourceTransactionManager;

    public TransactionUtil(PlatformTransactionManager dataSourceTransactionManager) {
        this.dataSourceTransactionManager = dataSourceTransactionManager;
    }

    private static Logger log = LoggerFactory.getLogger(TransactionUtil.class);

    private <T> T doTransactionTask(Callable<T> job, Runnable rollback, int transactionDefinition) {

        DefaultTransactionDefinition def = new DefaultTransactionDefinition(transactionDefinition);
        // def.setPropagationBehavior(transactionDefinition);
        TransactionStatus status = dataSourceTransactionManager.getTransaction(def);

        int code = def.hashCode();
        try {
            log.info("transaction starts: " + code);
            T t = job.call();
            dataSourceTransactionManager.commit(status);

            log.info("transaction end: " + code);
            return t;
        } catch (Throwable e) {

            dataSourceTransactionManager.rollback(status);

            log.info("transaction rollback: " + code);

            if (rollback != null) {
                log.error("transaction rollback, error: " + e.getMessage(), e);
                rollback.run();
            } else {

                if (e instanceof java.lang.reflect.InvocationTargetException) {
                    e = ((java.lang.reflect.InvocationTargetException) e).getTargetException();
                }

                // Exception
                log.error("transaction rollback, error: " + e.getMessage(), e);
                throw new BizException(e.getMessage());
            }

        }
        return null;
    }


    public <T> T doTransaction(Callable<T> job, Runnable rollback, int transactionDefinition) {
        return doTransactionTask(job, rollback, transactionDefinition);
    }

    public <T> T doTransaction(Callable<T> job, int transactionDefinition) {
        return doTransactionTask(job, null, transactionDefinition);
    }

    public <T> T doTransaction(Callable<T> job) {
        return doTransactionTask(job, null, TransactionDefinition.PROPAGATION_REQUIRED);
    }


    public TransactionStatus begin() {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = dataSourceTransactionManager.getTransaction(def);
        return status;
    }

    /**
     * begin
     *
     * @param transactionDefinition @see TransactionDefinition.PROPAGATION_REQUIRED
     * @return
     */
    public TransactionStatus begin(int transactionDefinition) {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setPropagationBehavior(transactionDefinition);
        TransactionStatus status = dataSourceTransactionManager.getTransaction(def);
        return status;
    }

    public void commit(TransactionStatus status) {
        dataSourceTransactionManager.commit(status);
    }

    public void rollback(TransactionStatus status) {
        dataSourceTransactionManager.rollback(status);
    }

    /**
     * 判断当前线程是否处于事物中，是就等事物提交后再执行，否则直接执行
     *
     * @param runnable
     */
    public static void executeAfterCommit(Runnable runnable) {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
                @Override
                public void afterCommit() {
                    runnable.run();
                }
            });
        } else {
            runnable.run();
        }
    }

}
