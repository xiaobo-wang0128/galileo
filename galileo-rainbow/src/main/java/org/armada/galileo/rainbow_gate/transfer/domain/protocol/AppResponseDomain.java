package org.armada.galileo.rainbow_gate.transfer.domain.protocol;

public class AppResponseDomain {

	/**
	 * 状态码
	 */
	private Integer code = 0;

	/**
	 * 业务执行耗费时间
	 */
	private Integer costTime = -1;

	/**
	 * 错误信息
	 */
	private String message;

	/**
	 * 返回的数据对象
	 */
	private Object result;

	/**
	 * 执行的远程ip
	 */
	private String remoteIp;

	public AppResponseDomain() {
	}

	/**
	 * <pre>
	  0     操作成功
	  1  	执行中
	  101   app 响应超时
	  102   app 连接失败
	  201   gateServer 响应超时
	  202   gateServer 连接失败
	  203   gateServer 认证失败
	  204   gateServer 业务执行失败
	  301 	gateClient 异常
	  500   appServer 端网络异常
	  501   appServer 端业务异常
	 * </pre>
	 * 
	 * @param code
	 */
	public AppResponseDomain(Integer code) {
		this.code = code;
	}

	/**
	 * <pre>
	  0     操作成功
	  1 	执行中
	  
	  200   gateServer 响应超时
	  
	  300 	gateClient 异常
	  
	  500   appServer端网络异常
	  501   appServer端业务异常
	 * </pre>
	 * 
	 */
	public Integer getCode() {
		return code;
	}

	public void setCode(Integer code) {
		this.code = code;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Object getResult() {
		return result;
	}

	public void setResult(Object result) {
		this.result = result;
	}

	public Integer getCostTime() {
		return costTime;
	}

	public void setCostTime(Integer costTime) {
		this.costTime = costTime;
	}

	public String getRemoteIp() {
		return remoteIp;
	}

	public void setRemoteIp(String remoteIp) {
		this.remoteIp = remoteIp;
	}
}
