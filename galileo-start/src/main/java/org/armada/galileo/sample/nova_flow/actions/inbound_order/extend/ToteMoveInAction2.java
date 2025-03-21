package org.armada.galileo.sample.nova_flow.actions.inbound_order.extend;

import com.hairoutech.common.dto.order.InboundOrderDTO;
import lombok.extern.slf4j.Slf4j;
import org.armada.galileo.nova_flow.annotation.FlowActionImpl;
import org.armada.galileo.nova_flow.domain.Action;

/**
 * 整箱上架
 *
 * @author xiaobo
 * @date 2021/10/8 6:29 下午
 */
@Slf4j
@FlowActionImpl( name="扩展实现整箱上架2", desc="用于测试2 ToteMoveInAction2" )
public class ToteMoveInAction2 {

	public void execute(Action action, InboundOrderDTO inboundOrderDTO) {
	}

}
