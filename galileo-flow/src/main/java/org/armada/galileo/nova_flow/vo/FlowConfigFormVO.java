package org.armada.galileo.nova_flow.vo;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FlowConfigFormVO {

	/**
	 * 流程定义大类 code
	 */
	private String flowDefineCode;

	/**
	 * 流程定义大类 name
	 */
	private String flowDefineName;

	/**
	 * 当前生效的配置文件
	 */
	private String flowDefineXmlPath;

	/**
	 * 流程配置可选项
	 */
	private List<FlowOption> flowConfigs;

	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	public static class FlowOption {

		/**
		 * 配置文件名
		 */
		private String name;

		/**
		 * xml配置文件 classpath , 不要以 / 开头
		 */
		private String xmlPath;

		/**
		 * action 可选项
		 */
		private List<ActionOption> actionOptions;

	}

	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	public static class ActionOption {

		/**
		 * 业务 Action 的 code
		 */
		private String actionCode;

		/**
		 * action名称
		 */
		private String actionName;

		/**
		 * 生效的实现类
		 */
		private String activeClass;

		/**
		 * action 实现类可选项
		 */
		private List<ActionImplOption> implOptions;

	}

	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	public static class ActionImplOption {

		private String implName;

		private String className;

		private String desc;
	}

}
