package org.armada.galileo.nova_flow.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;

/**
 * 状态机流程定义， 一个配置文件 对应一个对象
 */
@Data
@Accessors(chain = true)
public class ActionFlowDefine {

	/**
	 * 单据名称（流程名称）
	 */
	private String flowDefineName;

	/**
	 * 流程code
	 */
	private String flowDefineCode;

	/**
	 * 流程xml名称
	 */
	private String flowDefineXmlName;

	/**
	 * 流程xml路径
	 */
	private String flowDefineXmlPath;

	/**
	 * 业务类
	 */
	private String bizClass;

	/**
	 * 状态字段
	 */
	private String bizStatus;

	/**
	 * 所有 action集合（ actionCode:action 映射）
	 */
	private Map<String, Action> actionMap;

	/**
	 * 所有 状态集合
	 */
	private Map<String, Status> statusMap;

	/**
	 * statusCode: action 映射，缓存每个状态下唯一可进行的状态变更的操作
	 */
	private Map<String, List<Action>> statusActionMap;

	/**
	 * 开始节点
	 */
	private String startNode;

	/**
	 * 结束节点
	 */
	private List<String> endNode;

	/**
	 * 根据状态编码返回状态名
	 *
	 * @param statsCode
	 * @return
	 */
	public String getStatus(String statsCode) {
		Status s = getStatusMap().get(statsCode);
		if (s == null) {
			return "";
		}
		return s.getName();
	}

	/**
	 * 获取初始状态名
	 *
	 * @return
	 */
	public String getInitStatusCode() {
		Status s = getStatusMap().get(this.getStartNode());
		if (s == null) {
			throw new RuntimeException("案件初始化状态获取失败");
		}
		return s.getCode();
	}

	/**
	 * 获取当前操作的 下一个 操作
	 *
	 * @param actionDefine
	 * @return
	 */
	public Action getNextAction(Action actionDefine) {
		// 下一步操作
		List<Action> nextActions = getStatusActionMap().get(actionDefine.getTargetStatus());
		return nextActions != null && nextActions.size() > 0 ? nextActions.get(0) : null;

	}

	/**
	 * 根据 操作code 返回操作对象
	 *
	 * @param actionCode
	 * @return
	 */
	public Action getAction(String actionCode) {
		Action action = getActionMap().get(actionCode);
		return action;
	}

	@Data
	@Accessors(chain = true)
	public static class CommonConfig {
		private String name;
		private String code;
		private String value;// 作为后续扩展项
	}

	@Data
	@Accessors(chain = true)
	@AllArgsConstructor
	public static class Status {

		/**
		 * 状态名
		 */
		private String name;

		/**
		 * 状态编码
		 */
		private String code;

		/**
		 * 是否临时状态
		 */
		private boolean isTmp;

		/**
		 * 该状态下可进行的操作
		 */
		private List<String> action;

		public Status(String name, String code) {
			this.name = name;
			this.code = code;
		}
	}

}
