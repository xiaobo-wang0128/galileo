package org.armada.galileo.nova_flow.util;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.support.AbstractApplicationContext;

/**
 * @author xiaobo
 * @date 2021/10/26 2:48 下午
 */
public class FlowSpringUtil {

    private static AbstractApplicationContext applicationContext;

    public static void setApplicationContext(AbstractApplicationContext applicationContext) {
        FlowSpringUtil.applicationContext = applicationContext;
    }

    private static Object lock = new Object();

    /**
     * 从 spring 容器中获取一个bean, 如果 bean 不存在，则创建并缓存该bean
     *
     * @param cls
     * @param <T>
     * @return bean
     */
    public static <T> T createAndCacheBean(Class<T> cls) {

        T bean = null;
        try {
            bean = applicationContext.getBean(cls);
        } catch (NoSuchBeanDefinitionException e) {
            synchronized (lock) {
                bean = applicationContext.getAutowireCapableBeanFactory().createBean(cls);
                ConfigurableListableBeanFactory beanFactory = applicationContext.getBeanFactory();
                beanFactory.registerSingleton(cls.getName(), bean);
            }
        }

        return bean;
    }

}
