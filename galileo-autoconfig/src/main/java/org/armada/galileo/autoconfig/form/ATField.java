package org.armada.galileo.autoconfig.form;

import java.util.List;

import lombok.Data;

@Data
public class ATField {

	/**
	 * 字段名
	 */
	private String name;

	/**
	 * 描述信息
	 */
	private String desc;

	/**
	 * 字段编码
	 */
	private String code;

	/**
	 * 字段类型 text 文本框 textarea 文本域 select 下拉框 date 日期控件 datetime 时间控件 radio 单选
	 * checkbox 多选 file 文件上传 email 邮箱 phone 电话 number 数字 address 地址
	 */
	private String type;

	/**
	 * 字段最大长度（type == text/textarea）
	 */
	private Integer maxLen;

	/**
	 * 允许上传的文件类型， 逗号分隔（type == file）
	 */
	private String accept;

	/**
	 * 类型为 select checkbox 时的选项列表（type == select/checkbox/radio）
	 */
	private List<ATFieldOption> options;

	/**
	 * 最小值（type == number）
	 */
	private Integer min;

	/**
	 * 最大值（type == number）
	 */
	private Integer max;

	/**
	 * 正则表达式校验
	 */
	private String regex;

	/**
	 * 时间格式
	 */
	private String format;

	/**
	 * 是否多选 （type == select）y n
	 */
	private boolean multiple;

	/**
	 * 小数点位数
	 */
	private Integer precision;
	
	/**
	 * 是否为组合表单
	 */
	private boolean combined;
	
	/**
	 * 组合表单的配置项 json
	 */
	private String combinedFields;
	
	/**
	 * 是否只读属性（ combined 类型的表单中会用到）
	 */
	private boolean readonly;

	/**
	 * 是否显示滑块
	 */
	private boolean slider;

	/**
	 * vif
	 */
	private String vif;

	/**
	 * 是否可追回， 仅针对于 list<object> 类型
	 */
	private boolean append;
	
//
//	/**
//	 * 后缀
//	 */
//	private String suffix;
//
//	/**
//	 * 默认值
//	 */
//	private String defaultValue;
//
//	/**
//	 * 暗文
//	 */
//	private String placeholder;

}
