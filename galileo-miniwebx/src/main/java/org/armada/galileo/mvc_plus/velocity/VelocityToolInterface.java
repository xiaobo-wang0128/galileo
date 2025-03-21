package org.armada.galileo.mvc_plus.velocity;


import java.util.Map;

import javax.servlet.http.HttpServletRequest;


public interface VelocityToolInterface {

	/**
	 * 填充 controll 中需要用到的全局变量（工具类）
	 * 
	 * @param context
	 */
	public void fillMap(Map<String, Object> context);

	/**
	 * 填充 request 中需要用到的全局变量（工具类）
	 * 
	 * @param request
	 */
	public void fillRequest(HttpServletRequest request);
}
