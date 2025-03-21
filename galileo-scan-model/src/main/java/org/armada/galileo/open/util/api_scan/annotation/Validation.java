package org.armada.galileo.open.util.api_scan.annotation;


import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
public @interface Validation {

    /**
     * 是否允许为空（true: 允许空， false 不允许）<br/>
     * 默认 true 允许为空
     */
    public boolean allowNull() default true;

    /**
     * 最小长度（输入类型为字符串时校验）
     */
    public int minLen() default -1;

    /**
     * 最大长度（输入类型为字符串时校验）
     */
    public int maxLen() default -1;
}
