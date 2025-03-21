package org.armada.galileo.annotation.mvc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 此标记表示该接口不需要签名校验
 * 
 * @author xiaobowang
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.TYPE })
public @interface NoSign {
	// public LoginType type() default LoginType.Common;
}
