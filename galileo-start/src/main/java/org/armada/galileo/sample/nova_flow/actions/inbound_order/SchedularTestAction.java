package org.armada.galileo.sample.nova_flow.actions.inbound_order;

import lombok.extern.slf4j.Slf4j;
import org.armada.galileo.common.util.CommonUtil;
import org.armada.galileo.nova_flow.annotation.FlowActionImpl;
import org.armada.galileo.nova_flow.domain.Action;

import java.util.Date;

/**
 * 定时任务 test
 *
 * @author xiaobo
 * @date 2021/10/21 8:04 下午
 */
@Slf4j
@FlowActionImpl(name = "定时任务")
public class SchedularTestAction {

    public void execute(Action action) {
        //log.info("[auto task test] SchedularTestAction " + CommonUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss.S"));
    }

}
