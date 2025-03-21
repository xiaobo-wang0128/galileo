package org.armada.galileo.sample.nova_flow.actions.inbound_order;

import org.armada.galileo.nova_flow.annotation.FlowActionImpl;
import org.armada.galileo.nova_flow.domain.Action;

import com.hairoutech.common.dto.order.InboundOrderDTO;

import lombok.extern.slf4j.Slf4j;

/**
 * 创建入库订单
 *
 * @author xiaobo
 * @date 2021/10/8 6:27 下午
 */
@Slf4j
@FlowActionImpl( name="标准实现", desc="用于测试1")
public class CreateInboundAction {

	/**
	 * action实现类
	 * 
	 * @param action
	 * @param inboundOrderDTO
	 */
	public String execute(Action action, InboundOrderDTO inboundOrderDTO) {
		System.out.println("CreateInboundAction.execute");
		return "stand";
	}

}
