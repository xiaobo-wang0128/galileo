package org.armada.galileo.sample.web.rpc;

import org.armada.galileo.plugin.util.PluginSdkUtils;
import org.armada.galileo.sample.plugin_test.InboundTestPlugin;
import org.armada.galileo.sample.plugin_test.OutboundPlugin;
import org.springframework.stereotype.Controller;

/**
 * @author xiaobo
 * @date 2021/10/27 3:39 下午
 */
@Controller
public class PluginRpc {

    public Object test1() {
        InboundTestPlugin plugin = PluginSdkUtils.getPlugin(InboundTestPlugin.class);
        if (plugin == null) {
            return "plugin is disabled";
        }
        return plugin.doSomething();
    }

    public Object test2() {
        OutboundPlugin plugin = PluginSdkUtils.getPlugin(OutboundPlugin.class);
        if (plugin == null) {
            return "plugin is disabled";
        }
        return plugin.doSomething();
    }
}
