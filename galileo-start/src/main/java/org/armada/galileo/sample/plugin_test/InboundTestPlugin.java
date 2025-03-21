package org.armada.galileo.sample.plugin_test;

import org.armada.galileo.plugin.annotation.NovaPlugin;

/**
 * @author xiaobo
 * @description: TODO
 * @date 2021/10/27 3:22 下午
 */
@NovaPlugin(name = "入库插件", group = "入库")
public interface InboundTestPlugin {
    public String doSomething();
}
