package org.armada.galileo.sample.nova_flow.actions.inbound_order.extend;

import com.hairoutech.common.dto.order.InboundOrderDTO;
import lombok.extern.slf4j.Slf4j;
import org.armada.galileo.nova_flow.annotation.FlowActionImpl;
import org.armada.galileo.nova_flow.domain.Action;

/**
 * 创建入库订单
 *
 * @author xiaobo
 * @date 2021/10/8 6:27 下午
 */
@Slf4j
@FlowActionImpl(name = "扩展实现2", desc = "用于测试2 CreateInboundAction2")
public class CreateInboundAction2 {

    /**
     * action实现类
     *
     * @param action
     * @param inboundOrderDTO
     */
    public String execute(Action action, InboundOrderDTO inboundOrderDTO) {
        System.out.println("CreateInboundAction2.execute");
        return "222";
    }

}
