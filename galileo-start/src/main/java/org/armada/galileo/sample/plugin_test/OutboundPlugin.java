package org.armada.galileo.sample.plugin_test;

import org.armada.galileo.plugin.annotation.NovaPlugin;

/**
 * @author xiaobo
 * @description: TODO
 * @date 2021/10/27 3:22 下午
 */
@NovaPlugin(name = "出库配置", group = "入库", sort = 2)
public interface OutboundPlugin {
    public String doSomething();
}
