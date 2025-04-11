package org.armada.galileo.autoconfig.annotation;

import lombok.Data;

@Data
public class Option {

	/**
	 * 选项名
	 */
	private String label;

	/**
	 * 选项值
	 */
	private String value;

	/**
	 * 备注
	 */
	private String remark;

	public Option() {
	}

	public Option(String label, String value) {
		this.label = label;
		this.value = value;
	}

	public Option(String label, String value, String remark) {
		this.label = label;
		this.value = value;
		this.remark = remark;
	}

}
