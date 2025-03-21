package org.armada.galileo.sample.nova_flow.actions.inbound_order;

import org.armada.galileo.nova_flow.annotation.FlowActionImpl;
import org.armada.galileo.nova_flow.domain.Action;

import com.hairoutech.common.dto.order.InboundOrderDTO;

/**
 * 取消入库单
 * 
 * @author xiaobo
 * @date 2021/10/8 6:31 下午
 */
@FlowActionImpl( name="标准实现")
public class CancelInboundOrderAction {

	public void execute(Action action, InboundOrderDTO inboundOrderDTO) {

	}

}
