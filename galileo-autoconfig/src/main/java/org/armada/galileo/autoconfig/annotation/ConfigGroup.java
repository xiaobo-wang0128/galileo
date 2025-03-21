package org.armada.galileo.autoconfig.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({   ElementType.TYPE })
public @interface ConfigGroup {

	/**
	 * 配置名
	 * 
	 * @return
	 */
	String group() default "配置分组";

	/**
	 * 排序
	 * @return
	 */
	int sort() default 0;
	
	/**
	 * 描述信息.在前端做为 tooltip 展示
	 * 
	 * @return
	 */
	String desc() default "";

	/**
	 * 是否开启保存前的校验，当此配置项为 true 时， 需在配置类加入 public void preCheck() 方法，并将参数校验逻辑写到方法中，如果校验失败，在此方法中抛出异常即可
	 * @return
	 */
	boolean preCheck() default false;

	/**
	 * 是否开启配置修改成功后的通知，当开启此配置时，需在配置类中加入 public void afterModify() 方法，并在此方法中编写监听逻辑
	 * @return
	 */
	boolean afterModify() default false;
 

}
