package org.armada.spi.annotation;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Notify {

    /**
     * 消息分组字段
     *
     * @return
     */
    public String group() default "";

    /**
     * 排序字段，数字越大，优先级越高
     *
     * @return
     */
    public int sort() default 0;


}
