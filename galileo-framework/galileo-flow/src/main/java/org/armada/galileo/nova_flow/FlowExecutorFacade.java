package org.armada.galileo.nova_flow;

import lombok.extern.slf4j.Slf4j;
import org.armada.galileo.common.lock.ConcurrentLock;
import org.armada.galileo.common.util.CommonUtil;
import org.armada.galileo.exception.BizException;
import org.armada.galileo.nova_flow.domain.Action;
import org.armada.galileo.nova_flow.domain.ActionFlowDefine;
import org.armada.galileo.nova_flow.exception.FlowException;
import org.armada.galileo.nova_flow.util.FlowNacosUtil;
import org.armada.galileo.nova_flow.util.FlowSpringUtil;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * @author xiaobo
 * @date 2021/10/21 5:16 下午
 */
@Slf4j
public class FlowExecutorFacade {

    private StateActionFactory stateActionFactory;

    private static Executor ex = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() + 1);

    public FlowExecutorFacade(ApplicationContext applicationContext, FlowNacosUtil flowNacosUtil, ConcurrentLock flowLock) {

        FlowSpringUtil.setApplicationContext((AbstractApplicationContext) applicationContext);

        this.stateActionFactory = new StateActionFactory(applicationContext, flowNacosUtil, flowLock);

        // 将 stateActionFactory 注册到 spring 容器 （单例）
        AbstractApplicationContext context = (AbstractApplicationContext) applicationContext;
        context.getBeanFactory().registerSingleton("stateActionFactory", stateActionFactory);
    }

    /**
     * 检查实体对象当前状态能否进行相应的操作
     *
     * @param flowDefineCode 流程类型
     * @param actionCode     操作类code
     * @param bizObject      业务对象
     * @return
     */
    public boolean checkActionCanDo(String flowDefineCode, String actionCode, Object bizObject) {

        ActionFlowDefine stateActionFlow = stateActionFactory.getActiveStateActionFlow(flowDefineCode);

        if (stateActionFlow == null) {
            String err = "[nova-flow] 流程定义'" + flowDefineCode + "'不存在";
            log.error(err);
            throw new FlowException(err);
        }

        // 业务类
        String flowBizClass = stateActionFlow.getBizClass();

        // 状态字段
        String flowBizStatus = stateActionFlow.getBizStatus();

        Action actionDefine = stateActionFlow.getAction(actionCode);
        if (actionDefine == null) {
            throw new FlowException("[nova-flow] action 定义不存在：" + actionCode);
        }

        // 业务方法执行后的结果
        Object result = null;

        // 手动类型的任务
        if (actionDefine.getType().equals("manual")) {

            // 前置状态判断
            Class<?> bizClass = null;
            try {
                bizClass = Class.forName(flowBizClass);
            } catch (ClassNotFoundException e) {
                throw new BizException("无法找到实现类:" + flowBizClass);
            }
            Field field = ReflectionUtils.findField(bizClass, flowBizStatus);

            if (field == null) {
                throw new FlowException("状态字段不存在, class:{}, field:{} ", flowBizClass, flowBizStatus);
            }
            ReflectionUtils.makeAccessible(field);

            // 检查前置状态
            if (bizObject != null && !StringUtils.isEmpty(actionDefine.getBeforeStatus())) {

                if (bizObject instanceof Collection) {

                    for (Object obj : (Collection<?>) bizObject) {

                        Object statusObject = ReflectionUtils.getField(field, obj);

                        if (statusObject == null) {
                            statusObject = "NULL";
                            //throw new FlowException("[nova-flow] 状态字段值为空:" + flowBizStatus);
                        }

                        String currentStatus = statusObject.toString();

                        if (!currentStatus.equals(actionDefine.getBeforeStatus()) && !((actionDefine.getAllowStatus() != null && actionDefine.getAllowStatus().contains(currentStatus)))) {
                            return false;
                        }
                    }

                } else {

                    Object statusObject = ReflectionUtils.getField(field, bizObject);

                    if (statusObject == null) {
                        statusObject = "NULL";
                        // throw new FlowException("[nova-flow] 状态字段值为空:" + flowBizStatus);
                    }
                    String currentStatus = statusObject.toString();

                    if (!currentStatus.equals(actionDefine.getBeforeStatus()) && !((actionDefine.getAllowStatus() != null && actionDefine.getAllowStatus().contains(currentStatus)))) {
                        return false;
                    }

                }
            }

        }


        return true;
    }


    /**
     * 返回订单当前状态可进行的操作code列表
     *
     * @param flowDefineCode 流程类型
     * @param statusValue    当前状态值
     * @return
     */
    public List<String> checkActionCode(String flowDefineCode, String statusValue) {

        ActionFlowDefine stateActionFlow = stateActionFactory.getActiveStateActionFlow(flowDefineCode);

        if (stateActionFlow == null) {
            String err = "[nova-flow] 流程定义'" + flowDefineCode + "'不存在";
            log.error(err);
            throw new FlowException(err);
        }

        List<Action> actionList = stateActionFlow.getStatusActionMap().get(statusValue);

        if (CommonUtil.isNotEmpty(actionList)) {
            return actionList.stream().filter(e -> e.getType().equals("manual")).map(e -> e.getCode()).collect(Collectors.toList());
        }

        return null;

    }


    /**
     * 执行 action 方法
     *
     * @param flowDefineCode 流程定义code
     * @param actionCode     原子化操作 code
     * @param bizObject      业务入参
     * @param others         附加参数
     * @return
     */
    public Object doAction(String flowDefineCode, String actionCode, Object bizObject, Object... others) throws Exception {

        ActionFlowDefine stateActionFlow = stateActionFactory.getActiveStateActionFlow(flowDefineCode);

        if (stateActionFlow == null) {
            String err = "[nova-flow] 流程定义'" + flowDefineCode + "'不存在";
            log.error(err);
            throw new FlowException(err);
        }

        // 单据名称（流程名称）
        String flowDefineName = stateActionFlow.getFlowDefineName();

        // 流程xml路径
        String flowDefineXmlPath = stateActionFlow.getFlowDefineXmlPath();

        // 业务类
        String flowBizClass = stateActionFlow.getBizClass();

        // 状态字段
        String flowBizStatus = stateActionFlow.getBizStatus();

        Action actionDefine = stateActionFlow.getAction(actionCode);
        if (actionDefine == null) {
            throw new FlowException("[nova-flow] action 定义不存在：" + actionCode);
        }

        // 业务方法执行后的结果
        Object result = null;

        // 手动类型的任务
        if (actionDefine.getType().equals("manual")) {

            Class<?> targetClass = null;
            try {
                targetClass = Class.forName(actionDefine.getActiveClass());
            } catch (ClassNotFoundException e) {
                throw new BizException("无法加载class: " + actionDefine.getActiveClass());
            }

            Object handler = FlowSpringUtil.createAndCacheBean(targetClass);

            Method handlerMethod = getHandleMethod(targetClass);
            if (handlerMethod == null) {
                throw new FlowException("[nova-flow] 业务实现类必须有 execute 方法, class: " + actionDefine.getActiveClass());
            }

            // 前置状态判断
            Class<?> bizClass = null;
            try {
                bizClass = Class.forName(flowBizClass);
            } catch (ClassNotFoundException e) {
                throw new BizException("无法加载class: " + flowBizClass);
            }
            Field field = ReflectionUtils.findField(bizClass, flowBizStatus);

            if (field == null) {
                throw new FlowException("状态字段不存在, class:{}, field:{} ", flowBizClass, flowBizStatus);
            }
            ReflectionUtils.makeAccessible(field);

            // 检查前置状态
            if (bizObject != null && !StringUtils.isEmpty(actionDefine.getBeforeStatus())) {

                List<String> okStatus = actionDefine.getAllowStatus();

                if (bizObject instanceof Collection) {

                    for (Object obj : (Collection<?>) bizObject) {

                        Object statusObject = ReflectionUtils.getField(field, obj);
                        if (statusObject == null) {
                            statusObject = "NULL";
                            // throw new FlowException("[nova-flow] 状态字段值为空:" + flowBizStatus);
                        }
                        String currentStatus = statusObject.toString();
                        if (!currentStatus.equals(actionDefine.getBeforeStatus()) && !((actionDefine.getAllowStatus() != null && actionDefine.getAllowStatus().contains(currentStatus)))) {
                            log.warn("[nova-flow] 当前状态不允许进行该操作, current: {}, need: {}", currentStatus, CommonUtil.join(okStatus, ","));
                            throw new FlowException("当前状态不正确，无法进行该操作！（请刷新后重试）");
                        }
                    }

                } else {

                    Object statusObject = ReflectionUtils.getField(field, bizObject);

                    if (statusObject == null) {
                        statusObject = "NULL";
                    }
                    String currentStatus = statusObject.toString();
                    if (!currentStatus.equals(actionDefine.getBeforeStatus()) && !((actionDefine.getAllowStatus() != null && actionDefine.getAllowStatus().contains(currentStatus)))) {
                        log.warn("[nova-flow] 当前状态不允许进行该操作, current: {}, need: {}", currentStatus, CommonUtil.join(okStatus, ","));
                        throw new FlowException("当前状态不正确，无法进行该操作！（请刷新后重试）");
                    }
                }
            }

            // 构造方法参数
            Object[] args = null;
            if (others != null && others.length > 0) {
                args = new Object[2 + others.length];
                args[0] = actionDefine;
                args[1] = bizObject;
                for (int i = 0; i < others.length; i++) {
                    args[2 + i] = others[i];
                }
            } else {
                args = new Object[]{actionDefine, bizObject};
            }

            result = handlerMethod.invoke(handler, args);

            // 业务方法执行完成后，需判断下一步操作是否为 自动节点
            Action nextAction = stateActionFlow.getNextAction(actionDefine);

            if (nextAction != null && "auto".equals(nextAction.getType())) {

                try {
                    targetClass = Class.forName(nextAction.getActiveClass());
                } catch (ClassNotFoundException e) {
                    throw new BizException("无法加载class: " + nextAction.getActiveClass());
                }

                handler = FlowSpringUtil.createAndCacheBean(targetClass);

                handlerMethod = getHandleMethod(targetClass);
                if (handlerMethod == null) {
                    throw new FlowException("[nova-flow] 业务实现类必须有 execute 方法, class: " + nextAction.getActiveClass());
                }

                args[0] = nextAction;
                handlerMethod.invoke(handler, args);

            }
        }

        return result;
    }


    /**
     * 执行 action 方法，不做状态校验
     *
     * @param flowDefineCode 流程定义code
     * @param actionCode     原子化操作 code
     * @param bizObject      业务入参
     * @param others         附加参数
     * @return
     */
    public Object doActionWithoutCheck(String flowDefineCode, String actionCode, Object bizObject, Object... others) throws Exception {

        ActionFlowDefine stateActionFlow = stateActionFactory.getActiveStateActionFlow(flowDefineCode);

        if (stateActionFlow == null) {
            String err = "[nova-flow] 流程定义'" + flowDefineCode + "'不存在";
            log.error(err);
            throw new FlowException(err);
        }

        // 业务类
        String flowBizClass = stateActionFlow.getBizClass();

        // 状态字段
        String flowBizStatus = stateActionFlow.getBizStatus();

        Action actionDefine = stateActionFlow.getAction(actionCode);
        if (actionDefine == null) {
            throw new FlowException("[nova-flow] action 定义不存在：" + actionCode);
        }

        // 业务方法执行后的结果
        Object result = null;

        // 手动类型的任务
        if (actionDefine.getType().equals("manual")) {

            Class<?> targetClass = null;
            try {
                targetClass = Class.forName(actionDefine.getActiveClass());
            } catch (ClassNotFoundException e) {
                throw new BizException("无法加载class: " + actionDefine.getActiveClass());
            }

            Object handler = FlowSpringUtil.createAndCacheBean(targetClass);

            Method handlerMethod = getHandleMethod(targetClass);
            if (handlerMethod == null) {
                throw new FlowException("[nova-flow] 业务实现类必须有 execute 方法, class: " + actionDefine.getActiveClass());
            }

            // 前置状态判断
            Class<?> bizClass = null;
            try {
                bizClass = Class.forName(flowBizClass);
            } catch (ClassNotFoundException e) {
                throw new BizException("无法加载class: " + flowBizClass);
            }
            Field field = ReflectionUtils.findField(bizClass, flowBizStatus);

            if (field == null) {
                throw new FlowException("状态字段不存在, class:{}, field:{} ", flowBizClass, flowBizStatus);
            }
            ReflectionUtils.makeAccessible(field);

            // 构造方法参数
            Object[] args = null;
            if (others != null && others.length > 0) {
                args = new Object[2 + others.length];
                args[0] = actionDefine;
                args[1] = bizObject;
                for (int i = 0; i < others.length; i++) {
                    args[2 + i] = others[i];
                }
            } else {
                args = new Object[]{actionDefine, bizObject};
            }

            result = handlerMethod.invoke(handler, args);


        }

        return result;
    }


    private Method getHandleMethod(Class<?> targetClass) {
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
