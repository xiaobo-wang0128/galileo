package org.armada.galileo.annotation.mvc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 保存参数标识注解
 * 
 * @author xieqinghua
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface PagingConfig {
	/**
	 * 生成分页url时，需要保留的参数
	 * 
	 * @return
	 */
	public String[] defaultParams() default {};

	/**
	 * 是否需要附加 defaultParams 以外的不确定的url参数
	 * 
	 * @return
	 */
	public boolean extraParams() default false;

	/**
	 * 当extraParams为true时，是否只保存那个 key、value形同id格式的参数， 默认为true
	 * 
	 * @return
	 */
	public boolean just4Ids() default false;

	/**
	 * 是否去除重复的参数
	 * 
	 * @return
	 */
	public boolean ignoreRepeatParam() default false;

	/**
	 * 对于重复的参数，截取哪一个 first: 取第一个 last: 取最后一个
	 * 
	 * @return
	 */
	public String repeatChoose() default "last";

	public int pageSize() default -1;

}