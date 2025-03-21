package org.armada.galileo.nova_flow.vo;

import java.util.List;

import lombok.Data;

/**
 * flow 流程切换输入参数
 * 
 * @author wangxiaobo
 * @date 2021年10月22日
 */

@Data
public class FlowSwithVO {
	
	/**
	 * 流程定义大类 code
	 */
	private String flowDefineCode;

	/**
	 * 当前生效的配置文件
	 */
	private String flowDefineXmlPath;

	/**
	 * 当前生效的 class 业务类
	 */
	private List<ActiveClass> actionActiveClass;

	
	@Data
	public class ActiveClass {

		/**
		 * 业务 Action 的 code
		 */
		private String actionCode;

		/**
		 * 生效的实现类
		 */
		private String activeClass;

	}

}
