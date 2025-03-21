package org.armada.galileo.rainbow_gate.transfer.client;

/**
 * 异常处理接口
 * 
 * @author xiaobowang
 *
 */
public interface ExceptionHandler {

	/**
	 * 异常处理
	 * 
	 * <pre>
	  300   超时
	  400   网络错误
	  404   接口不存在
	  500   后台业务异常（已捕获）
	  501   后台系统异常（未捕获）
	  601   未登陆
	  602   权限异常
	 * </pre>
	 * 
	 * @param errorCode 错误码
	 * @param errorMessage 错误信息
	 * @param className 调用的接口名称
	 * @param methodName 方法名
	 */
	public void handle(int errorCode, String errorMessage, String className, String methodName);
}
