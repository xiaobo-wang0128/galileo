package org.armada.galileo.autoconfig.form;

import java.util.List;

import lombok.Data;

@Data
public class ATFieldOption {

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

	/**
	 * 子对象
	 */
	private List<SubOption> children;

	@Data
	public static class SubOption {
		/**
		 * 选项名
		 */
		private String label;

		/**
		 * 选项值
		 */
		private String value;

	}

}
