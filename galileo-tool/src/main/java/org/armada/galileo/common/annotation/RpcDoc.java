package org.armada.galileo.common.annotation;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
public @interface RpcDoc {

    /**
     * 排序，由小到大
     */
    public int sort() default 9999;

    /**
     * 分组
     */
    public String group() default "默认";
}
