package org.armada.galileo.open.util;

import com.google.gson.reflect.TypeToken;
import org.armada.galileo.common.util.JsonUtil;
import org.armada.galileo.common.util.ParameterNameDiscovererCache;
import org.armada.galileo.exception.BizException;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Map;

/**
 * @author xiaobo
 * @date 2021/11/18 10:31 下午
 */
public class ParamUtil {

    public static Object[] transferRequestJson(String clsName, String methodName, String jsonContent) {


        Class<?> cls = null;
        Method method = null;

        try {
            cls = Class.forName(clsName);
        } catch (Exception e) {
            throw new BizException("无法加载class: " + clsName);
        }
        Method[] methods = cls.getMethods();
        for (Method m : methods) {
            if (m.getName().equals(methodName)) {
                method = m;
                break;
            }
        }
        if (method == null) {
            throw new BizException("没有找到待执行方法: " + clsName + "." + methodName);
        }

        Object[] args = null;

        Type[] inputTypes = method.getGenericParameterTypes();
        if (inputTypes != null && inputTypes.length > 0) {

            // 多个参数 需要从map中 找到对应值
            if (inputTypes.length > 1) {
                String[] paramNames = ParameterNameDiscovererCache.getParameterNameDiscoverer().getParameterNames(method);

                args = new Object[paramNames.length];
                Map<String, Object> paramMap = JsonUtil.fromJson(jsonContent, new TypeToken<Map<String, Object>>() {
                }.getType());
                for (int i = 0; i < paramNames.length; i++) {
                    Object tmpArg = paramMap.get(paramNames[i]);
                    if (tmpArg == null) {
                        args[i] = null;
                    } else {
                        args[i] = JsonUtil.fromJson(JsonUtil.toJson(tmpArg), inputTypes[i]);
                    }
                }

            } else {
                Object arg0 = JsonUtil.fromJson(jsonContent, inputTypes[0]);
                args = new Object[]{arg0};
            }
        }

        return args;
    }
}
