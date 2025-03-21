package org.armada.galileo.config;

import org.armada.galileo.common.loader.SpringMvcUtil;
import org.armada.galileo.rainbow_gate.transfer.gate_point.app_server.RainbowRpcInit;
import org.armada.galileo.rainbow_gate.transfer.gate_point.register.RegisterUtil;
import org.springframework.beans.BeansException;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author xiaobo
 * @date 2021/6/2 8:53 上午
 */

@Configuration
public class RainbowServiceConfig implements ApplicationContextAware {


    // -----  以下配置请勿修改 -------

    /**
     * 注册 AppServer端 Servlet
     *
     * @return
     */
    @Bean
    public ServletRegistrationBean initAppServerServlet() {
        return RegisterUtil.getAppServerServlet();
    }

    /**
     * 监听 rainbow-client 地址、 app-server 地址
     */
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {

        SpringMvcUtil.setContext(applicationContext);

        // RainbowRpcInit.initIndex("sampe-app", "zookeeper://nova-zookeeper:2181");

        RainbowRpcInit.init("sampe-app", "nacos://bronze-nacos:8848");

        //注册监听器，解决用户上下文传递问题
//        RegisterUtil.registProviderInterceptor(new ProviderInterceptor());
//        RegisterUtil.registConsumerInterceptor(new ConsumerInterceptor());
    }
}
