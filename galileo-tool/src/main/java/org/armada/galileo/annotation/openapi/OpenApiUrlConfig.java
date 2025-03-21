package org.armada.galileo.annotation.openapi;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE})
public @interface OpenApiUrlConfig {
    /**
     * 在开放平台暴露的地址前缀
     * @return
     */
    public String urlHead();


    /**
     * 在开放平台暴露的应用名， 会显示在开放平台接口文档页
     * @return
     */
    public String appDesc();


    /**
     * 在 rainbow 服务中的名称，一般为 xxx-api， 用于服务注册名
     * @return
     */
    public String appName();



}
