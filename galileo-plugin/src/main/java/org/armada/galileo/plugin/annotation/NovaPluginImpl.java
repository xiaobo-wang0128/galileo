package org.armada.galileo.plugin.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface NovaPluginImpl {

    /**
     * 插件名称
     *
     * @return
     */
    String name();

    /**
     * 插件名称的详细描述
     *
     * @return
     */
    String desc() default "";

    /**
     * 排序
     *
     * @return
     */
    int sort() default 0;

}
