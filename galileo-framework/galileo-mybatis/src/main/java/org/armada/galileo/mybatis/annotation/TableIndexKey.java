package org.armada.galileo.mybatis.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface TableIndexKey {

    String[] value() default {""};

    public boolean unique() default false;

    public boolean fullIndex() default false;
}
