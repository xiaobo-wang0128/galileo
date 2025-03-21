package org.armada.galileo.sample.plugin_test.impl;

import org.armada.galileo.plugin.annotation.NovaPluginImpl;
import org.armada.galileo.sample.plugin_test.OutboundPlugin;

import java.util.Date;

/**
 * @author xiaobo
 * @date 2021/10/27 7:47 下午
 */
@NovaPluginImpl(name = "出库插件1", desc = "出库插件用于测试1", sort = 1)
public class OutboundPluginImpl1 implements OutboundPlugin {
    @Override
    public String doSomething() {
        return "OutboundPluginImpl1:" + new Date();
    }
}
