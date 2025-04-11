package org.armada.galileo.nova_flow.util;

import org.armada.galileo.common.lock.ConcurrentLock;
import org.armada.galileo.nova_flow.domain.Action;
import org.armada.galileo.nova_flow.domain.FlowJobTask;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 定时任务工具类
 */
public class JobUtil {

    private static Logger log = LoggerFactory.getLogger(JobUtil.class);

    private static Scheduler scheduler;

    private static AtomicBoolean hasInit = new AtomicBoolean(false);

    static {
        try {

            scheduler = StdSchedulerFactory.getDefaultScheduler();
            scheduler.start();

        } catch (SchedulerException e) {
            log.error(e.getMessage(), e);
        }
    }


    private static Map<String, Boolean> exist = new ConcurrentHashMap<>();

    /**
     * 添加并开始任务
     */
    public static void start(Action action, ConcurrentLock flowLock) {

        String taskKey = action.getCode() + "_" + action.getCode() + action.getActiveClass();

        synchronized (log) {
            if (exist.get(taskKey) != null && exist.get(taskKey)) {
                throw new RuntimeException("[nova-flow] 定时任务定义重复, actionCode:" + action.getCode() + ", class:" + action.getActiveClass());
            }
            exist.put(taskKey, true);
        }

        try {

            String expression = action.getExpression();

            JobKey jobKey = new JobKey(action.getCode() + "_" + action.getActiveClass(), Scheduler.DEFAULT_GROUP);
            TriggerKey triggerKey = null;

            JobDetail jd = scheduler.getJobDetail(jobKey);
            if (jd != null) {
                scheduler.deleteJob(jobKey);
            }

            JobDataMap newJobDataMa = new JobDataMap();
            newJobDataMa.put("action", action);
            newJobDataMa.put("flowLock", flowLock);

            JobDetail job = JobBuilder.newJob(FlowJobTask.class).withIdentity(jobKey).setJobData(newJobDataMa).build();

            Trigger trigger = TriggerBuilder.newTrigger().withIdentity(triggerKey).withSchedule(CronScheduleBuilder.cronSchedule(expression)).build();

            scheduler.scheduleJob(job, trigger);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

    }

}
