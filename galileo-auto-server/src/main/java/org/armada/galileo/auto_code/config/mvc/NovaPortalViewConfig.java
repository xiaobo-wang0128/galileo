package org.armada.galileo.auto_code.config.mvc;

import org.armada.galileo.common.util.CommonUtil;
import org.armada.galileo.mvc_plus.converter.*;
import org.armada.galileo.mvc_plus.support.MiniWebxServlet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.support.FormattingConversionServiceFactoryBean;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * nova-config 前端 proxy 配置类
 */
@Configuration
public class NovaPortalViewConfig {

    /**
     * uri sevlet容器相对路径
     */
    private String contextPath = "galileo-auto";

    @Autowired
    private ApplicationContext applicationContext;


    @Bean
    public FormattingConversionServiceFactoryBean initMiniwebxConversion() {
        FormattingConversionServiceFactoryBean conversion = new FormattingConversionServiceFactoryBean();
        Set<Converter<?, ?>> converters = new HashSet<Converter<?, ?>>();
        converters.add(new String2DateConverter(CommonUtil.asList("yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm", "yyyy-MM-dd")));
        converters.add(new String2DoubleConverter());
        converters.add(new String2IntegerConverter());
        converters.add(new String2BigDecimalConverter());
        converters.add(new String2LongConverter());
        conversion.setConverters(converters);
        return conversion;
    }

    @Bean
    public ServletRegistrationBean initMiniwebx() {
        String modulePath = "org.armada.galileo";

        List<HandlerInterceptor> interceptors = new ArrayList<HandlerInterceptor>();

        MiniWebxServlet servlet = new MiniWebxServlet(modulePath, contextPath, applicationContext, interceptors);
        return new ServletRegistrationBean<MiniWebxServlet>(servlet, "/", "*.json", "*.htm", "/statics/*");

    }


}
