package org.armada.galileo.plugin.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface NovaPlugin {

    /**
     * 插件名称
     *
     * @return
     */
    String name();

    /**
     * 插件所在的分组
     *
     * @return
     */
    String group();

    /**
     * 插件名称的详细描述
     *
     * @return
     */
    String desc() default "";

    /**
     * 用于控制前端显示顺序 (从小到大排列)
     *
     * @return
     */
    int sort() default 0;

}
