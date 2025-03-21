package org.armada.galileo.plugin.web.rpc;

import com.google.common.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.armada.galileo.common.util.CommonUtil;
import org.armada.galileo.common.util.JsonUtil;
import org.armada.galileo.plugin.util.NovaPluginBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author xiaobo
 * @date 2021/10/27 5:45 下午
 */

@Controller
@Slf4j
public class NovaPluginRpc {

    @Autowired
    private ApplicationContext applicationContext;

    public Map<String, Object> getConfig() {
        NovaPluginBean bean = applicationContext.getBean(NovaPluginBean.class);

        Map<String, Object> map = new HashMap<>();
        map.put("form", bean.getAllGroups());
        map.put("value", bean.getCurrentConfig());

        return map;
    }

    public void swithConfig(HttpServletRequest request) {
        String json = CommonUtil.readJsonForm(request);
        NovaPluginBean bean = applicationContext.getBean(NovaPluginBean.class);
        // 获取远程配置，用于覆盖本地配置
        List<NovaPluginBean.RemotePluginConfig> remoteConfigs = JsonUtil.fromJson(json, new com.google.gson.reflect.TypeToken<List<NovaPluginBean.RemotePluginConfig>>() {
        }.getType());
        bean.updateLocalConfig(remoteConfigs);
    }

}