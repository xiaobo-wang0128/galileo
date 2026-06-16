package org.armada.galileo.mybatis.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface TableColumn {


    /**
     * 字段长度仅针对 varchar 有效
     *
     * @return
     */
    public int len() default 255;

    /**
     * 字段精度，仅针对 decimal 类型
     *
     * @return
     */
    public int precision() default 2;

    /**
     * 字段注释
     *
     * @return
     */
    public String comment() default "";

    /**
     * 字段默认值
     *
     * @return
     */
    String defaultValue() default "";

    /**
     * 是否允许为空
     *
     * @return
     */
    boolean notNull() default false;

    /**
     * 字段类型映射（默认不需要配置，会根据字段类型映射成 mysql 类型）
     *
     * @return
     */
    String type() default "";


}
