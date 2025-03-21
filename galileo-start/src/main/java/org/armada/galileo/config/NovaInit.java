package org.armada.galileo.config;

import org.armada.galileo.autoconfig.AutoConfigBean;
import org.armada.galileo.nova_flow.FlowExecutorFacade;
import org.armada.galileo.nova_flow.util.FlowNacosUtil;
import org.armada.galileo.nova_flow.util.FlowSpringUtil;
import org.armada.galileo.plugin.util.NovaPluginBean;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.AbstractApplicationContext;

/**
 * @author xiaobo
 * @date 2021/10/27 5:50 下午
 */
@Configuration
public class NovaInit  implements ApplicationContextAware {

    @Autowired
    private ApplicationContext applicationContext;

    @Bean
    public FlowExecutorFacade getFlowExecutorUtil() {
        // FlowNacosUtil flowNacosUtil = new FlowNacosUtil("bronze-nacos:8848", "galileo");
        return new FlowExecutorFacade(applicationContext, null, null);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        FlowSpringUtil.setApplicationContext((AbstractApplicationContext) applicationContext);

//        FlowNacosUtil flowNacosUtil = new FlowNacosUtil("bronze-nacos:8848", "galileo");
//        FlowExecutorFacade facade = new FlowExecutorFacade(applicationContext, flowNacosUtil, null);
//
//        // 将 stateActionFactory 注册到 spring 容器 （单例）
//        AbstractApplicationContext context = (AbstractApplicationContext) applicationContext;
//        context.getBeanFactory().registerSingleton("flowExecutorFacade", facade);
    }

    @Bean
    public NovaPluginBean initNovaPluginBean() {
        return new NovaPluginBean("bronze-nacos:8848", "galileo");
    }

    @Bean
    public AutoConfigBean initNacosUtil() {
        return new AutoConfigBean("bronze-nacos:8848", "galileo");
    }


}
