package org.armada.galileo.nova_flow.domain;

import java.lang.reflect.Method;

import org.armada.galileo.common.lock.ConcurrentLock;
import org.armada.galileo.nova_flow.exception.FlowException;
import org.armada.galileo.nova_flow.util.FlowSpringUtil;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FlowJobTask implements Job {

    private static Logger log = LoggerFactory.getLogger(FlowJobTask.class);

    private ConcurrentLock flowLock;

    public FlowJobTask() {
    }

    public void execute(JobExecutionContext context) throws JobExecutionException {

        JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();

        Action action = (Action) jobDataMap.get("action");
        this.flowLock = (ConcurrentLock) jobDataMap.get("flowLock");

        try {
            // String fireTime = FlowCommonUtil.format(context.getFireTime(), "yyyy-MM-dd HH:mm:ss");

            log.info("[nova-flow] 开始执行定时任务, actionCode:{}, targetClass:{}", action.getName(), action.getActiveClass());

            String key = new StringBuilder("nova_flow_task_").append(action.getCode()).append("_").append(action.getActiveClass()).toString();

            if (flowLock != null && !flowLock.lock(key, null)) {
                log.warn("[nova-flow] 定时任务获取锁失败，忽略本次调用, actionCode:{}, targetClass:{}", action.getName(), action.getActiveClass());
                return;
            }


            Object taskObject = FlowSpringUtil.createAndCacheBean(Class.forName(action.getActiveClass()));

            try {

                Method handlerMethod = getHandleMethod(taskObject.getClass());
                Class<?>[] paramTypes = handlerMethod.getParameterTypes();
                Object[] args = new Object[paramTypes.length];
                for (int i = 0; i < args.length; i++) {
                    if (paramTypes[i].getName().equals(Action.class.getName())) {
                        args[i] = action;
                        continue;
                    }
                }
                handlerMethod.invoke(taskObject, args);

            } catch (Exception e) {
                throw new FlowException(e);
            } finally {
                if (flowLock != null) {
                    flowLock.unlock(key);
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    private static Method getHandleMethod(Class<?> targetClass) {
        Method handlerMethod = null;
        Method[] methods = targetClass.getMethods();
        for (Method method : methods) {
            if (method.getName().equals("execute")) {
                handlerMethod = method;
                break;
            }
        }
        return handlerMethod;
    }
}