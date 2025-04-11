package org.armada.galileo.annotation.openapi;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE})
public @interface OpenApi {

//    /**
//     * 接口类型
//     *
//     * @return
//     */
//    public OpenApiType apiType();

    /**
     * 接口所在的业务模块分组
     * @return
     */
    public String group();

    /**
     * 在文档系统中的排序， 从小到大
     */
    public int sort() default -1;

    /**
     * 调用方
     * @return
     */
    ApiRole apiFrom();

    /**
     * 被调用方
     * @return
     */
    ApiRole apiTo();



}
