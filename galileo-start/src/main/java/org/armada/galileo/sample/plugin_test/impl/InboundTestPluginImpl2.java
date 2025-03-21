package org.armada.galileo.sample.plugin_test.impl;

import org.armada.galileo.common.util.CommonUtil;
import org.armada.galileo.plugin.annotation.NovaPluginImpl;
import org.armada.galileo.sample.plugin_test.InboundTestPlugin;

import java.util.Date;

/**
 * @author xiaobo
 * @date 2021/10/27 3:22 下午
 */
@NovaPluginImpl(name = "插件2", desc = "入库用于测试2")
public class InboundTestPluginImpl2 implements InboundTestPlugin {

    @Override
    public String doSomething() {

        System.out.println("plugin 2");

        return "InboundTestPluginImpl2 + " + CommonUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss");
    }

}
