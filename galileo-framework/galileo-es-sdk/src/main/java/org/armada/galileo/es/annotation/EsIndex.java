package org.armada.galileo.es.annotation;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface EsIndex {

    /**
     * 是否针对当前字段开启全文索引
     * @return
     */
    public boolean fullIndex() default false;

}
