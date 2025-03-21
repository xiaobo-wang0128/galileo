package org.armada.spi.open.util;


/**
 * 远程调用返回结果
 *
 * @author xiaobo
 * @date 2021/4/23 10:07 上午
 */
public class HaiqActionResult {

	/**
	 * 
	 * <pre>
	  0:    操作成功
	  300   超时
	  400   网络错误
	  404   接口不存在
	  500   后台业务异常（已捕获）
	  501   后台系统异常（未捕获）
	  601   未登陆
	  602   权限异常
	 * </pre>
	 * 
	 */
	private Integer code = 0;

	// 返回的数据对象
	private Object data;

	// 错误信息
	private String message;

	public HaiqActionResult() {
	}

	public HaiqActionResult(Object result) {
		this.data = result;
	}

	public Integer getCode() {
		return code;
	}

	public void setCode(Integer code) {
		this.code = code;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
