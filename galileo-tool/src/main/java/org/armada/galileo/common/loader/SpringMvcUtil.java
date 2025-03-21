package org.armada.galileo.common.loader;

import org.springframework.context.ApplicationContext;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;

public class SpringMvcUtil {

    private static ApplicationContext context;

    /**
     * 获取 beanfactory 对象
     *
     * @return
     */
    public static ApplicationContext getContext() {
        return context;
    }

    public static void setContext(ApplicationContext context) {
        SpringMvcUtil.context = context;
    }

    @SuppressWarnings("unchecked")
    public synchronized static <T> T getBean(String beanName) {
        return (T) context.getBean(beanName);
    }

    public static <T> T getBean(Class<T> class1) {
        return context.getBean(class1);
    }


}
