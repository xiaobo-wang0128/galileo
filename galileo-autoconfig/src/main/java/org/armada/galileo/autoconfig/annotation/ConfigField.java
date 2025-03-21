package org.armada.galileo.autoconfig.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.TYPE, ElementType.FIELD })
public @interface ConfigField {

	/**
	 * 配置名
	 * 
	 * @return
	 */
	String name() default "配置名";

	/**
	 * 描述信息.在前端做为 tooltip 展示
	 * 
	 * @return
	 */
	String desc() default "";

	/**
	 * 是否只读
	 * 
	 * @return
	 */
	boolean readonly() default false;

	/**
	 * 是否只允许编辑，不允许增加、删除配置项（仅针对 List 类型配置）
	 * 
	 * @return
	 */
	boolean editonly() default false;

	/**
	 * 可选项的 class, 必须是 ConfigOption 的实现类 <font color='red'>（ class 声明必须是 public 类型 ）</font>
	 * 
	 * @return
	 */
	Class<? extends ConfigOption> options() default ConfigOption.class;

	/**
	 * 是否支持多选 type=select 时有效
	 * 
	 * @return
	 */
	// boolean multiple() default false;

	/**
	 * 字段最大长度
	 * 
	 * @return
	 */
	int maxLen() default 512;

	/**
	 * 最小值 针对 number有效
	 * 
	 * @return
	 */
	int min() default 0;

	/**
	 * 最大值 针对 number有效
	 * 
	 * @return
	 */
	int max() default Integer.MAX_VALUE;

	/**
	 * 正则表达式校验 针对 string 有效
	 * 
	 * @return
	 */
	String regex() default "";

	//
	/**
	 * 时间格式，针对 Date 有效
	 * 
	 * @return
	 */
	String timeFormat() default "yyyy-MM-dd";

	/**
	 * 精度（小数点位数）
	 * 
	 * @return
	 */
	int precision() default 0;

	/**
	 * 是否多行文本（仅针对 string 类型有效）
	 * 
	 * @return
	 */
	boolean singleLine() default true;

	/**
	 * 针对数字类型，如果有 min、max, 是否显示滑块
	 *
	 * @return
	 */
	boolean slider() default false;


	/**
	 * 依赖项字段，只有依赖的配置项为 true 时，才会展示当前配置项
	 * @return
	 */
	String vif() default  "";


	/**
	 * 是否可追回配置项， 仅针对于 list<object> 类型
	 */
	boolean append() default false;


}
