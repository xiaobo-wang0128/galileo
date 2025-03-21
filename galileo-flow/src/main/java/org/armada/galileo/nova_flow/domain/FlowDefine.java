package org.armada.galileo.nova_flow.domain;

import lombok.Data;

import java.util.List;

/**
 * 流程定义大类
 * 
 * @author xiaobo
 * @date 2021/10/21 3:56 下午
 */
@Data
public class FlowDefine {

	/**
	 * 单据名称（流程名称）
	 */
	private String name;

	/**
	 * 流程code
	 */
	private String code;

	/**
	 * 业务类
	 */
	private String bizClass;

	/**
	 * 状态字段
	 */
	private String bizStatus;

	/**
	 * 流程定义文件路径
	 */
	private List<Flow> flowDefine;

	@Data
	public static class Flow {

		/**
		 * 配置文件名
		 */
		private String name;

		/**
		 * xml配置文件 classpath , 不要以 / 开头
		 */
		private String xmlPath;
	}

}
