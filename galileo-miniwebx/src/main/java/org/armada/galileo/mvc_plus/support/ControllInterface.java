package org.armada.galileo.mvc_plus.support;


import java.util.Map;

public interface ControllInterface {

	/**
	 * 在这里执行 controll 页面需要处理的业务
	 * 
	 * @param context
	 *            从页面中接收的参数的对象，该对象中的值 会传递到页面中
	 */
	public void execute(Map<String, Object> context);
}
