package org.armada.galileo.config;


import org.armada.galileo.nova_flow.FlowExecutorFacade;
import org.armada.galileo.sample.nova_flow.FlowEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

/**
 * @author xiaobo
 * @date 2021/10/8 8:05 下午
 */
@Service
@Slf4j
public class FlowActionExecutor {

    @Autowired
    private FlowExecutorFacade flowExecutorFacade;

    /**
     * 执行原子性操作
     *
     * @param flowEnum   流程类型
     * @param actionCode action编码（ action类名除去Action的部分）
     * @param bizObject  主业务对象
     * @param others     其他附加对象
     * @return 返回 action 类中 execute 方法的返回值
     */
    public Object doAction(FlowEnum flowEnum, String actionCode, Object bizObject, Object... others) throws Exception {
        return flowExecutorFacade.doAction(flowEnum.toString(), actionCode, bizObject, others);

    }

}
