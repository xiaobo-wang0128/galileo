package org.armada.galileo.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author xiaobo
 * @date 2021/11/5 11:23 下午
 */

@Configuration
public class ApiSdkConfig {

    @Autowired
    private ApplicationContext applicationContext;


    @Autowired
    private org.armada.galileo.util.JedisUtil jedisUtil;


//    @Bean
//    public ServletRegistrationBean initOpenApiRestServlet() {

//        // 接口配置持久化处理类
//
//        // 注册 接口配置持久化类到 spring 容器
//        AbstractApplicationContext context = (AbstractApplicationContext) applicationContext;
//        context.getBeanFactory().registerSingleton("sdk.sdkService", sdkService);
//
//        // 注册 haiqToCustomer 代理接口 到 spring 容器
//        // CallBackApi1 callBackApi1 = HttpOpenApiCallbackProxy.getProxy(CallBackApi1.class, sdkService);
//        // context.getBeanFactory().registerSingleton(CallBackApi1.class.getName(), callBackApi1);
//
//        // 注册 customerToHaiq servlet 至 spring-boot， 用于监听客户的请求
//        String apiHead = "/open/api";
//        HttpOpenApServlet servlet = new HttpOpenApServlet(applicationContext, apiHead, sdkService);
//        return new ServletRegistrationBean<HttpOpenApServlet>(servlet, apiHead + "/*");
    // }

}
