package org.armada.galileo.sample.web.rpc;

import com.hairoutech.common.dto.order.InboundOrderDTO;
import org.armada.galileo.config.FlowActionExecutor;
import org.armada.galileo.nova_flow.FlowExecutorFacade;
import org.armada.galileo.nova_flow.StateActionFactory;
import org.armada.galileo.sample.nova_flow.FlowEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

/**
 * @author xiaobo
 * @date 2021/10/26 2:00 下午
 */
@Controller
public class FlowTestRpc {

    @Autowired
    private FlowActionExecutor flowActionExecutor;

    @Autowired
    private StateActionFactory stateActionFactory;

    public Object getAllConfig() {
        return stateActionFactory.getAllStateActionFlowMap();
    }

    public Object doAction1(String flow, String action) throws Exception {

        InboundOrderDTO dto = new InboundOrderDTO();
        dto.setInboundOrderStatus("NEW");
        return flowActionExecutor.doAction(FlowEnum.Inbound, "StartMoveIn", dto);
    }


    public Object doAction2(String flow, String action)  throws Exception{

        InboundOrderDTO dto = new InboundOrderDTO();
        dto.setInboundOrderStatus("RECEIVING");
        return flowActionExecutor.doAction(FlowEnum.Inbound, "StartMoveIn", dto);
    }


    public Object doAction3() throws Exception {

        InboundOrderDTO dto = new InboundOrderDTO();
        dto.setInboundOrderStatus("RECEIVED");
        return flowActionExecutor.doAction(FlowEnum.Inbound, "StartMoveIn", dto);
    }
}
