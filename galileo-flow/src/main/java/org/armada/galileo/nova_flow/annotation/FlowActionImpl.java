package org.armada.galileo.nova_flow.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
public @interface FlowActionImpl {

	/**
	 * 原子化操作action 实现类的名称（简单的功能描述）
	 * 
	 * @return
	 */
	String name();

	/**
	 * 原子化操作action 实现类的详细描述
	 * 
	 * @return
	 */
	String desc() default "";

}
