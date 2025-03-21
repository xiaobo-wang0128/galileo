package org.armada.galileo.plugin.util;

/**
 * @author xiaobo
 * @date 2021/10/27 3:38 下午
 */
public class PluginSdkUtils {

    public static <T> T getPlugin(Class<T> cls){
        return NovaPluginBean.getActiveClassBean(cls);
    }
}
