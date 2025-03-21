package org.armada.spi.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface Validation {

    /**
     * 是否必填项
     */
    public boolean notNull() default true;

    /**
     * 最小长度（输入类型为字符串时校验）
     */
    public int minLen() default -1;

    /**
     * 最大长度（输入类型为字符串时校验）
     */
    public int maxLen() default -1;
}
