package org.armada.galileo.common.util;

import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.StandardReflectionParameterNameDiscoverer;

/**
 * @author xiaobo
 * @date 2023/2/23 16:54
 */
public class ParameterNameDiscovererCache {

    private static ParameterNameDiscoverer parameterNameDiscovererCache = new LocalVariableTableParameterNameDiscoverer();

//    private static ParameterNameDiscoverer parameterNameDiscovererCache = new StandardReflectionParameterNameDiscoverer();

    public static ParameterNameDiscoverer getParameterNameDiscoverer() {
        return parameterNameDiscovererCache;
    }

}
