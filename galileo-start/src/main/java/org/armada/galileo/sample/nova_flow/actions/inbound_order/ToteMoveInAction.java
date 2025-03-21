package org.armada.galileo.sample.nova_flow.actions.inbound_order;

import org.armada.galileo.nova_flow.annotation.FlowActionImpl;
import org.armada.galileo.nova_flow.domain.Action;

import com.hairoutech.common.dto.order.InboundOrderDTO;

import lombok.extern.slf4j.Slf4j;

/**
 * 整箱上架
 *
 * @author xiaobo
 * @date 2021/10/8 6:29 下午
 */
@Slf4j
@FlowActionImpl( name="标准整箱上架")
public class ToteMoveInAction {

	public void execute(Action action, InboundOrderDTO inboundOrderDTO) {
	}

}
