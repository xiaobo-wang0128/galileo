package org.armada.galileo.nova_flow.web.rpc;

import com.google.common.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.armada.galileo.common.util.CommonUtil;
import org.armada.galileo.common.util.JsonUtil;
import org.armada.galileo.nova_flow.StateActionFactory;
import org.armada.galileo.nova_flow.vo.FlowConfigFormVO;
import org.armada.galileo.nova_flow.vo.FlowSwithVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Controller
@Slf4j
public class NovaFlowRpc {

    @Autowired
    private ApplicationContext applicationContext;

    public List<FlowConfigFormVO> getConfig() {
        StateActionFactory stateActionFactory = applicationContext.getBean(StateActionFactory.class);
        return stateActionFactory.getConfigList();
    }

    public void swithConfig(HttpServletRequest request) {
        String json = CommonUtil.readJsonForm(request);
        List<FlowSwithVO> flowSwithList = JsonUtil.fromJson(json, new TypeToken<List<FlowSwithVO>>() {
        }.getType());
        StateActionFactory stateActionFactory = applicationContext.getBean(StateActionFactory.class);
        stateActionFactory.switchFlow(flowSwithList);
    }

}
