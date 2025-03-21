package org.armada.galileo.nova_flow;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.armada.galileo.common.lock.ConcurrentLock;
import org.armada.galileo.common.util.AssertUtil;
import org.armada.galileo.common.util.CommonUtil;
import org.armada.galileo.common.util.JsonUtil;
import org.armada.galileo.nova_flow.annotation.FlowActionImpl;
import org.armada.galileo.nova_flow.domain.Action;
import org.armada.galileo.nova_flow.domain.Action.ActionClassImpl;
import org.armada.galileo.nova_flow.domain.ActionFlowDefine;
import org.armada.galileo.nova_flow.domain.FlowDefine;
import org.armada.galileo.nova_flow.exception.FlowException;
import org.armada.galileo.common.loader.FileLoader;
import org.armada.galileo.nova_flow.util.FlowNacosUtil;
import org.armada.galileo.nova_flow.util.JobUtil;
import org.armada.galileo.nova_flow.vo.FlowConfigFormVO;
import org.armada.galileo.nova_flow.vo.FlowSwithVO;
import org.armada.galileo.nova_flow.vo.FlowConfigFormVO.ActionOption;
import org.armada.galileo.nova_flow.vo.FlowConfigFormVO.FlowOption;
import org.armada.galileo.nova_flow.vo.FlowSwithVO.ActiveClass;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.context.ApplicationContext;
import org.springframework.util.Assert;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import lombok.extern.slf4j.Slf4j;

/**
 * 流程配置文件读取、校验、初始化
 */
@Slf4j
public class StateActionFactory {

    /**
     * 缓存所有的 flow xml配置信息
     */
    private final Map<String, List<ActionFlowDefine>> allStateActionFlowMap = new ConcurrentHashMap<>();

    /**
     * 缓存生效中的 flow xml配置信息
     */
    private final Map<String, ActionFlowDefine> activeStateActionFlowMap = new ConcurrentHashMap<>();

    /**
     * 所有流程定义
     */
    private final List<FlowDefine> allFlowDefines = new ArrayList<>();

    /**
     * 自动定时任务
     */
    private final List<Action> scheduleTaskActions = new ArrayList<Action>();

    private static final AtomicBoolean hasInit = new AtomicBoolean(false);

    /**
     * spring 上下文对象
     */
    private ApplicationContext applicationContext;

    /**
     * 锁对象，由使用方实现
     */
    private ConcurrentLock flowLock;

    /**
     * nacos 远程存储
     */
    private FlowNacosUtil flowNacosUtil;

    public StateActionFactory(ApplicationContext applicationContext, FlowNacosUtil flowNacosUtil, ConcurrentLock flowLock) {
        this.applicationContext = applicationContext;
        this.flowLock = flowLock;
        // this.flowNacosUtil = flowNacosUtil;

        // 初始化
        this.init();

        // 切换配置
//        if (flowNacosUtil != null) {
//            String remoteConfig = flowNacosUtil.readAllFormValue();
//            List<FlowSwithVO> flowSwithList = JsonUtil.fromJson(remoteConfig, new com.google.common.reflect.TypeToken<List<FlowSwithVO>>() {
//            }.getType());
//            this.switchFlow(flowSwithList);
//
//            // 监听远程配置变化
//            try {
//                flowNacosUtil.watchFormValueChange(new FlowNacosUtil.NacosListener() {
//                    @Override
//                    public void afterChange(String nacosConfigs) {
//                        if (CommonUtil.isEmpty(nacosConfigs)) {
//                            return;
//                        }
//                        log.info("[nova-flow] 远程配置变更，准备更新：" + nacosConfigs);
//                        List<FlowSwithVO> flowSwithList = JsonUtil.fromJson(nacosConfigs, new com.google.common.reflect.TypeToken<List<FlowSwithVO>>() {
//                        }.getType());
//                        switchFlow(flowSwithList);
//                    }
//                });
//            } catch (Exception e) {
//                log.error(e.getMessage(), e);
//            }
//        }

        // 启动定时任务
        this.initTask();
    }

    /**
     * 获取可切换的配置信息
     *
     * @return
     */
    public List<FlowConfigFormVO> getConfigList() {

        List<FlowConfigFormVO> result = new ArrayList<>();

        for (FlowDefine flowDefine : allFlowDefines) {

            FlowConfigFormVO vo = new FlowConfigFormVO();

            vo.setFlowDefineCode(flowDefine.getCode());

            ActionFlowDefine activeFlowDefine = activeStateActionFlowMap.get(flowDefine.getCode());
            if (activeFlowDefine == null) {
                throw new RuntimeException("active flow define is null");
            }
            vo.setFlowDefineXmlPath(activeFlowDefine.getFlowDefineXmlPath());

            List<FlowOption> flowConfigs = allStateActionFlowMap.get(flowDefine.getCode()).stream().map(actionFlowDefine -> {

                String flowOptionName = actionFlowDefine.getFlowDefineXmlName();

                String flowOptioXmlPath = actionFlowDefine.getFlowDefineXmlPath();

                List<ActionOption> flowOptionActionActiveClass = actionFlowDefine.getActionMap().entrySet().stream().map(e -> {
                    //业务 Action 的 code
                    String actionOptionActionCode = e.getValue().getCode();
                    //action名称
                    String actionOptionActionName = e.getValue().getName();
                    //生效的实现类
                    String actionOptionActiveClass = e.getValue().getActiveClass();
                    // action 实现类可选项
                    List<FlowConfigFormVO.ActionImplOption> actionOptionImplOptions = e.getValue().getActionClassImpls()
                            .stream().map(e3 -> {

                                String implOptionImplName = e3.getName();
                                String implOptionClassName = e3.getClassName();
                                String desc = e3.getDesc();
                                return new FlowConfigFormVO.ActionImplOption(implOptionImplName, implOptionClassName, desc);

                            }).collect(Collectors.toList());

                    return new ActionOption(actionOptionActionCode, actionOptionActionName, actionOptionActiveClass, actionOptionImplOptions);

                }).collect(Collectors.toList());

                return new FlowOption(flowOptionName, flowOptioXmlPath, flowOptionActionActiveClass);

            }).collect(Collectors.toList());

            result.add(new FlowConfigFormVO(flowDefine.getCode(), flowDefine.getName(), activeFlowDefine.getFlowDefineXmlPath(), flowConfigs));
        }

        return result;
    }

    /**
     * 切换运行时的 流程配置
     *
     * @param flowSwithList
     */
    public void switchFlow(List<FlowSwithVO> flowSwithList) {

        if (flowSwithList == null || flowSwithList.isEmpty()) {
            return;
        }

        // 当流程配置调整后，远程存储的配置可能匹配不上，需要将不可用的配置项清除
        for (Iterator<FlowSwithVO> main = flowSwithList.iterator(); main.hasNext(); ) {

            FlowSwithVO flowSwithVO = main.next();

            List<ActionFlowDefine> existFlowDefines = allStateActionFlowMap.get(flowSwithVO.getFlowDefineCode());
            if (existFlowDefines == null || existFlowDefines.isEmpty()) {
                main.remove();
                continue;
            }

            ActionFlowDefine actionFlowDefine = existFlowDefines.stream().filter(e -> e.getFlowDefineXmlPath().equals(flowSwithVO.getFlowDefineXmlPath())).findFirst().orElse(null);

            if (actionFlowDefine == null) {
                log.info("[nova-flow] 修改配置时，配置不匹配, flowDefineCode:{}, flowDefineXmlPath: {}", flowSwithVO.getFlowDefineCode(), flowSwithVO.getFlowDefineXmlPath());
                main.remove();
                continue;
            }

            ActionFlowDefine tmpCurrentFlow = activeStateActionFlowMap.get(flowSwithVO.getFlowDefineCode());

            // 在与当前配置不同的情况下，需要执行切换
            if (tmpCurrentFlow == null || tmpCurrentFlow.getFlowDefineXmlPath() == null || !tmpCurrentFlow.getFlowDefineXmlPath().equals(actionFlowDefine.getFlowDefineXmlPath())) {
                activeStateActionFlowMap.put(actionFlowDefine.getFlowDefineCode(), actionFlowDefine);
                log.info("[nova-flow]【{}】流程配置切换成功，当前生效流程 flowName:{}, xmlPath:{} ", actionFlowDefine.getFlowDefineName(), actionFlowDefine.getFlowDefineXmlName(), actionFlowDefine.getFlowDefineXmlPath());
            }

            // action class 实现类切换
            List<ActiveClass> actionActiveClass = flowSwithVO.getActionActiveClass();

            if (actionActiveClass != null && actionActiveClass.size() > 0) {

                for (Iterator<ActiveClass> it = actionActiveClass.iterator(); it.hasNext(); ) {
                    ActiveClass actionConfig = it.next();
                    Action action = actionFlowDefine.getAction(actionConfig.getActionCode());
                    if (action == null) {
                        it.remove();
                        continue;
                    }

                    boolean matched = action.getActionClassImpls().stream().anyMatch(e -> e.getClassName().equals(actionConfig.getActiveClass()));
                    if (matched && !action.getActiveClass().equals(actionConfig.getActiveClass())) {
                        action.setActiveClass(actionConfig.getActiveClass());
                        log.info("[nova-flow] 流程:{}, Action:{}, 实现类切换成功，当前实现类:{} ", actionFlowDefine.getFlowDefineName(), action.getName(), action.getActiveClass());
                    }
                }

            }
        }

        if (flowNacosUtil != null) {
            flowNacosUtil.updateFormDefines(JsonUtil.toJsonPretty(flowSwithList));
        }
    }

    public void init() {

        log.info("[nova-flow] start init .... ");

        if (hasInit.compareAndSet(false, true)) {

            try {
                GsonBuilder gonsBuilder = new GsonBuilder();

                List<byte[]> flowExports = FileLoader.loadResourceFiles("galileo_flow/_flow_.json");
                if (flowExports != null && flowExports.size() > 0) {

                    for (byte[] bufs : flowExports) {

                        String jsonContent = new String(bufs, "utf-8");
                        Gson gson = gonsBuilder.create();
                        List<FlowDefine> flowDefines = gson.fromJson(jsonContent, new TypeToken<List<FlowDefine>>() {
                        }.getType());

                        for (FlowDefine flowDefine : flowDefines) {
                            String statusField = flowDefine.getBizClass();
                            int index = statusField.indexOf("->");
                            if (index == -1) {
                                throw new RuntimeException("配置不正确");
                            }

                            flowDefine.setBizClass(statusField.substring(0, index));
                            flowDefine.setBizStatus(statusField.substring(index + 2));

                            List<ActionFlowDefine> list = new ArrayList<>();
                            for (FlowDefine.Flow flowObj : flowDefine.getFlowDefine()) {

                                ActionFlowDefine actionFlowDefine = load(flowObj.getName(), flowObj.getXmlPath());

                                list.add(actionFlowDefine);

                                actionFlowDefine.setFlowDefineCode(flowDefine.getCode());
                                actionFlowDefine.setFlowDefineName(flowDefine.getName());

                                actionFlowDefine.setFlowDefineXmlName(flowObj.getName());
                                actionFlowDefine.setFlowDefineXmlPath(flowObj.getXmlPath());

                                actionFlowDefine.setBizClass(flowDefine.getBizClass());
                                actionFlowDefine.setBizStatus(flowDefine.getBizStatus());

                            }

                            allStateActionFlowMap.put(flowDefine.getCode(), list);
                            // 默认第1个生效
                            activeStateActionFlowMap.put(flowDefine.getCode(), list.get(0));

                        }

                        allFlowDefines.addAll(flowDefines);
                    }
                }

                gonsBuilder = null;

                flowExports = null;

            } catch (Exception e) {

                log.error(e.getMessage(), e);
                System.exit(0);

            }
        }
    }

    public ActionFlowDefine getActiveStateActionFlow(String flowDefine) {
        return activeStateActionFlowMap.get(flowDefine);
    }

    private ActionFlowDefine load(String flowName, String filePatch) {

        byte[] bytes = FileLoader.loadResourceFile(filePatch);
        log.info("[nova-flow] 加载配置文件: " + filePatch);

        if (bytes == null) {
            throw new FlowException("[nova-flow] 流程配置文件读取失败: " + filePatch);
        }

        SAXReader saxReader = new SAXReader();
        Document document = null;
        try {
            document = saxReader.read(new InputStreamReader(new ByteArrayInputStream(bytes), StandardCharsets.UTF_8));
        } catch (DocumentException e) {
            e.printStackTrace();
        }

        Element root = document.getRootElement();

        // 开始状态
        String startNodeStatus = null;
        // 结束状态
        List<String> endNodeStatus = new ArrayList<String>();

        // 读取状态定义
        List<String> allStatus = new ArrayList<String>();
        List<Element> statusDefines = root.element("status-define").elements();
        Map<String, ActionFlowDefine.Status> statusMap = new LinkedHashMap<>();
        for (Element element : statusDefines) {

            String name = element.attributeValue("name");
            String code = element.attributeValue("code");

            AssertUtil.isNotNull(name, "name");
            AssertUtil.isNotNull(code, "code");

            ActionFlowDefine.Status status = new ActionFlowDefine.Status(name, code);

            if (statusMap.get(status.getCode()) != null) {
                throw new RuntimeException("[nova-flow] 状态定义重复: " + status.getCode());
            }

            statusMap.put(status.getCode(), status);

            String type = element.attributeValue("type");
            if ("start".equals(type)) {
                startNodeStatus = code;
            }
            if ("end".equals(type)) {
                endNodeStatus.add(code);
            }

            allStatus.add(code);
        }

        if (startNodeStatus == null) {
            throw new RuntimeException("[nova-flow] 缺少起点状态节点配置");
        }
        if (endNodeStatus.size() == 0L) {
            throw new RuntimeException("[nova-flow] 缺少终结状态节点配置");
        }

        // 读取action定义
        // List<Action> actions = new ArrayList<Action>();

        // actionCode:action 映射
        Map<String, Action> actionMap = new LinkedHashMap<>();

        // statusCode:action 映射，缓存每个状态下唯一可进行的操作
        Map<String, List<Action>> statusActionMap = new LinkedHashMap<>();

        List<Element> actionDefines = root.element("action-define").elements();

        for (Element element : actionDefines) {

            String name = element.attributeValue("name");
            String actionCode = element.attributeValue("code");
            String type = element.attributeValue("type");
            String expression = element.attributeValue("expression");

            String targetClass = element.attributeValue("targetClass");
            String beforeStatus = element.attributeValue("beforeStatus");
            String targetStatus = element.attributeValue("targetStatus");
            String exceptionStatus = element.attributeValue("exceptionStatus");

            List<ActionClassImpl> actionClassImpls = new ArrayList<ActionClassImpl>();
            String activeClass = null;
            List<String> allowStatus = new ArrayList<>();

            if (beforeStatus == null || beforeStatus.matches("\\s*")) {
                beforeStatus = null;
            }
            if (targetStatus == null || targetStatus.matches("\\s*")) {
                targetStatus = null;
            }
            if (exceptionStatus == null || exceptionStatus.matches("\\s*")) {
                exceptionStatus = null;
            }

            if (targetStatus != null && exceptionStatus != null && exceptionStatus.equals(targetStatus)) {
                throw new RuntimeException("[nova-flow] action配置错误，targetStatus 与 exceptionStatus不能相同，actionName: " + name);
            }

            // 同时存在多个前置状态
            List<String> beforeList = CommonUtil.split(beforeStatus, ",");
            if (CommonUtil.isNotEmpty(beforeList)) {
                beforeList = beforeList.stream().distinct().collect(Collectors.toList());
                beforeStatus = beforeList.get(0);
                allowStatus = beforeList;
            }

            // 解析action 实现类
            if (StringUtils.isNotEmpty(targetClass)) {

                String[] tmpsClassList = targetClass.split(",");

                for (String tmpCls : tmpsClassList) {

                    if (CommonUtil.isEmpty(tmpCls)) {
                        continue;
                    }

                    tmpCls = tmpCls.trim();

                    // 实现类的名称
                    String tmpActionName = null;
                    // 实现类的详情描述
                    String tmpActionDesc = null;
                    // 目标实现类 classname
                    String tmpActionClassName = tmpCls;

                    try {
                        Class<?> tmpTargetCls = Class.forName(tmpActionClassName);
                        if (tmpTargetCls.isAnnotationPresent(FlowActionImpl.class)) {
                            FlowActionImpl anno = tmpTargetCls.getAnnotation(FlowActionImpl.class);

                            tmpActionName = anno.name();
                            tmpActionDesc = anno.desc();
                        }

                    } catch (Exception e) {
                        throw new RuntimeException("[nova-flow] actionClass 实现类不存在: " + tmpCls + ", actionName: " + name);
                    }

                    actionClassImpls.add(new ActionClassImpl(tmpActionName, tmpActionDesc, tmpActionClassName));
                }

                // 一个action 有多个实现的情况， 需要每个action有明确注释说明， 以更于前端配置
                if (actionClassImpls.size() > 1) {
                    for (ActionClassImpl impl : actionClassImpls) {
                        if (CommonUtil.isEmpty(impl.getName())) {

                            String error = "[nova-flow]流程配置 " + filePatch + "中 " + impl.getClassName() + " 必须添加 @FlowActionImpl 注解，用于描述用途方便前端切换配置";
                            throw new RuntimeException(error);
                        }
                    }
                }
                // 默认实现类为第一个
                activeClass = actionClassImpls.get(0).getClassName();

            } else {
                log.warn("[nova-flow] actionName {} 的实现类没有定义", name);
            }
            if ("auto".equals(type)) {
                if (StringUtils.isEmpty(beforeStatus)) {
                    throw new RuntimeException("[nova-flow]action 必须配置 beforeStatus， 用于前置状态判断, actionName: " + name);
                }

                if (StringUtils.isEmpty(exceptionStatus)) {
                    throw new RuntimeException("[nova-flow]自动类型的 action 必须配置 exceptionStatus， 用于失败后的状态回滚, actionName: " + name);
                }
            }

            if (CommonUtil.isEmpty(actionCode)) {
                // action code 直接取 class 名称 （去掉 className 的 Action 后缀）
                String tmpStr = activeClass.substring(activeClass.lastIndexOf(".") + 1);
                if (tmpStr.toLowerCase().endsWith("action")) {
                    tmpStr = tmpStr.substring(0, tmpStr.length() - 6);
                }
                actionCode = tmpStr;
            }

            Action action = new Action(name, type, expression, actionCode, actionClassImpls, activeClass, allowStatus, beforeStatus, targetStatus, exceptionStatus);

            Assert.notNull(action.getName(), "name");
            Assert.notNull(action.getCode(), "code");

            if (actionMap.get(action.getCode()) != null) {
                log.error("action定义重复: " + action.getCode());
                throw new RuntimeException("action定义重复: " + action.getCode());
            }

            actionMap.put(action.getCode(), action);


            if(CommonUtil.isNotEmpty(allowStatus)){
                for (String status : allowStatus) {
                    List<Action> actionList = statusActionMap.get(status);
                    if (actionList == null) {
                        actionList = new ArrayList<>();
                        statusActionMap.put(status, actionList);
                    }

                    List<String> existActionCodes = actionList.stream().map(e->e.getCode()).collect(Collectors.toList());

                    if(!existActionCodes.contains(action.getCode())){
                        actionList.add(action);
                    }
                }
            }

//            if (action.getBeforeStatus() != null) {
//
//                List<Action> actionList = statusActionMap.get(action.getBeforeStatus());
//                if (actionList == null) {
//                    actionList = new ArrayList<>();
//                    statusActionMap.put(action.getBeforeStatus(), actionList);
//                }
//
//                actionList.add(action);
//
//                // actions.add(action);
//            }

            if ("task".equals(action.getType())) {
                scheduleTaskActions.add(action);
            }
        }

        // checkLinkClose(flowName, actions, startNodeStatus, endNodeStatus, statusMap);

        ActionFlowDefine stateActionFlow = new ActionFlowDefine();
        stateActionFlow.setEndNode(endNodeStatus);
        stateActionFlow.setStartNode(startNodeStatus);
        stateActionFlow.setActionMap(actionMap);
        stateActionFlow.setStatusMap(statusMap);
        stateActionFlow.setStatusActionMap(statusActionMap);


        return stateActionFlow;
    }

    private static AtomicBoolean taskHasInit = new AtomicBoolean(false);

    public void initTask() {

        if (scheduleTaskActions == null || scheduleTaskActions.isEmpty()) {
            return;
        }

        if (!taskHasInit.compareAndSet(false, true)) {
            return;
        }

        if (applicationContext != null) {

            new Thread(() -> {
                // 等待外部资源加载完毕， 再启动定时任务
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                }

                // 开始启动 type==task 类型的定时任务
                for (Action action : scheduleTaskActions) {

                    if (StringUtils.isNotEmpty(action.getExpression())) {
                        try {
                            String expression = getConfigValue(action.getExpression());

                            action.setExpression(expression);
                            JobUtil.start(action, flowLock);

                            log.info("[nova-flow] 启动定时任务，任务名: {}, 类名: {}, 时间表达式: {}", action.getName(), action.getActiveClass(), action.getExpression());
                        } catch (Exception e) {
                            log.error("[nova-flow] 定时任务启动异常");
                            log.error(e.getMessage(), e);

                            System.exit(0);
                        }
                    }
                }
            }).start();
        } else {
            log.warn("applicationContext没有注入，将无法启动自动定时任务节点");
        }
    }


    private static void checkLinkClose(String flowName, List<Action> actions, String startStatus, List<String> endStatus, Map<String, ActionFlowDefine.Status> statusMap) {

        List<Node> allNodes = new ArrayList<>();

        for (Action action : actions) {

            // 前置状态
            if (CommonUtil.isNotEmpty(action.getBeforeStatus())) {

                List<String> beforeStatusList = CommonUtil.asList(action.getBeforeStatus());

                if (action.getAllowStatus() != null && action.getAllowStatus().size() > 0) {
                    beforeStatusList.addAll(action.getAllowStatus());
                }

                // 后置状态
                if (CommonUtil.isNotEmpty(action.getTargetStatus())) {
                    for (String str : beforeStatusList) {

                        ActionFlowDefine.Status s1 = statusMap.get(str);
                        ActionFlowDefine.Status s2 = statusMap.get(action.getTargetStatus());

                        if (!"NULL".equals(s1) && s1 == null) {
                            throw new FlowException("[nova-flow] 状态未定义：" + str);
                        }
                        if (s2 == null) {
                            throw new FlowException("[nova-flow] 状态未定义：" + action.getTargetStatus());
                        }

                        allNodes.add(new Node(s1.getCode(), s1.getName(), s2.getCode(), s2.getName()));
                    }
                }
                // 异常状态
                if (CommonUtil.isNotEmpty(action.getExceptionStatus())) {

                    // 后置状态
//                    if (CommonUtil.isEmpty(action.getTargetStatus())) {
//                        log.error("[nova-flow] 如果配置了 exceptionStatus，则 targetStatus 不允许为空");
//                        System.exit(0);
//                    }

                    for (String str : beforeStatusList) {

                        ActionFlowDefine.Status s1 = statusMap.get(str);
                        ActionFlowDefine.Status s2 = statusMap.get(action.getExceptionStatus());

                        if (s1 == null) {
                            throw new FlowException("[nova-flow] 状态未定义：" + str);
                        }
                        if (s2 == null) {
                            throw new FlowException("[nova-flow] 状态未定义：" + action.getTargetStatus());
                        }

                        allNodes.add(new Node(s1.getCode(), s1.getName(), s2.getCode(), s2.getName()));

                    }
                }
            }
        }

        // 去重
        allNodes = allNodes.stream().distinct().filter(e -> !e.pre.equals(e.next)).collect(Collectors.toList());


        // 去重
        allNodes = allNodes.stream().distinct().filter(e -> !e.pre.equals(e.next)).collect(Collectors.toList());


        BlockingDeque<List<Node>> links = new LinkedBlockingDeque<>();

        for (Iterator<Node> it = allNodes.iterator(); it.hasNext(); ) {
            Node node = it.next();

            if (startStatus.equals(node.getPre())) {
                List<Node> link = new ArrayList<>();

                link.add(node);
                links.add(link);

                it.remove();
            }
        }


        List<List<Node>> allExistLinks = new ArrayList<>();


        while (true) {

            List<Node> link = links.poll();

            if (link == null) {
                break;
            }

            List<Node> snapshot = new ArrayList<>(allNodes);

            while (true) {

                int before = snapshot.size();

                tmp:
                for (Iterator<Node> it = snapshot.iterator(); it.hasNext(); ) {

                    Node n = it.next();

                    try {
                        for (Node tmpNode : link) {
                            if (n.equals(tmpNode)) {
                                it.remove();
                                continue tmp;
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    List<String> pres = link.stream().map(e -> e.pre).collect(Collectors.toList());

                    if (n.getPre().equals(link.get(link.size() - 1).next)) {
                        if (!pres.contains(n.next)) {   // || !pres.contains(n.pre)
                            link.add(n);
                        }
                        try {
                            it.remove();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        continue;
                    }
                }


                int after = snapshot.size();

                if (before != after) {
                    continue;
                }
                if (before == after) {

                    List<String> ss = link.stream().map(e -> e.pre).collect(Collectors.toList());
                    ss.add(link.get(link.size() - 1).next);

                    if (!endStatus.contains(link.get(link.size() - 1).next)) {

                        log.warn(flowName + "流程未闭环：" + CommonUtil.join(ss, " -> "));

                        //throw new FlowException("流程未闭环：" + CommonUtil.join(ss, " -> "));
                    } else {
                        // System.out.println("流程：" + CommonUtil.join(ss, " -> "));
                        allExistLinks.add(link);
                    }

                    if (snapshot.size() > 0) {

                        List<List<Node>> ntmpLinks = new ArrayList<>();

                        //System.out.println("snapshot: " + JsonUtil.toJson(snapshot));

                        for (List<Node> tmpLink : allExistLinks) {
                            List<String> tmpSlist = tmpLink.stream().map(e -> e.next).collect(Collectors.toList());

                            for (Iterator<Node> tmpIt = snapshot.iterator(); tmpIt.hasNext(); ) {
                                Node n = tmpIt.next();
                                if (tmpSlist.contains(n.pre) && tmpSlist.contains(n.next)) {
                                    tmpIt.remove();
                                    continue;
                                }
                            }
                        }

                        for (List<Node> tmpLink : links) {
                            List<String> tmpSlist = tmpLink.stream().map(e -> e.next).collect(Collectors.toList());

                            for (Iterator<Node> tmpIt = snapshot.iterator(); tmpIt.hasNext(); ) {
                                Node n = tmpIt.next();
                                if (tmpSlist.contains(n.pre) && tmpSlist.contains(n.next)) {
                                    tmpIt.remove();
                                    continue;
                                }
                            }
                        }

                        for (Iterator<Node> tmpIt = snapshot.iterator(); tmpIt.hasNext(); ) {

                            Node n = tmpIt.next();


                            for (int i = link.size() - 1; i >= 0; i--) {

                                if (n.pre.equals(link.get(i).next)) {

                                    List<Node> tmpNewLink = new ArrayList<>(link.subList(0, i + 1));
                                    tmpNewLink.add(n);

                                    if (endStatus.contains(tmpNewLink.get(tmpNewLink.size() - 1).next)) {
                                        ntmpLinks.add(tmpNewLink);
                                    } else {
                                        links.push(tmpNewLink);
                                    }
                                }
                            }
                        }

                        if (ntmpLinks != null && ntmpLinks.size() > 0) {
                            allExistLinks.addAll(ntmpLinks);
                        }
                    }

                    break;
                }

            }
        }

        for (List<Node> link : allExistLinks) {
            List<String> ss = link.stream().map(e -> e.preCn).collect(Collectors.toList());
            ss.add(link.get(link.size() - 1).nextCn);
            log.info("[nova-flow] " + flowName + ": " + CommonUtil.join(ss, "->"));
        }
    }


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    private static class Node {
        public String pre;
        public String preCn;

        public String next;
        public String nextCn;


        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Node node = (Node) o;
            return Objects.equals(pre, node.pre) && Objects.equals(next, node.next);
        }

        public String toString() {
            return pre + ">" + next;
        }
    }


    /**
     * 变量替换
     */
    private String getConfigValue(String configValue) {
        if (configValue.startsWith("${") && configValue.endsWith("}")) {
            configValue = configValue.substring(2, configValue.length() - 1);

            String[] tmps = configValue.split(":");

            String key = tmps[0].trim();
            String value = null;
            if (tmps.length == 2) {
                value = tmps[1].trim();
            }

            if (applicationContext != null) {
                String v = applicationContext.getEnvironment().getProperty(key);
                if (v != null) {
                    value = v.trim();
                }
            }
            return value;
        }
        return configValue;
    }

    public Map<String, List<ActionFlowDefine>> getAllStateActionFlowMap() {
        return allStateActionFlowMap;
    }
}
