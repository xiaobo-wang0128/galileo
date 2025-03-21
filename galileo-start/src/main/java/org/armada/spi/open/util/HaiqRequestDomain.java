package org.armada.spi.open.util;


/**
 * 请求参数封装
 *
 * @author xiaobo
 * @date 2021/4/23 10:07 上午
 */
public class HaiqRequestDomain {

	/**
	 * 接口class name
	 */
	private String className;

	/**
	 * 目标方法名
	 */
	private String methodName;

	/**
	 * 入参类型
	 */
	private String[] paramTypeNames;

	/**
	 * 入参
	 */
	private Object[] paramInputs;

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	public String[] getParamTypeNames() {
		return paramTypeNames;
	}

	public void setParamTypeNames(String[] paramTypeNames) {
		this.paramTypeNames = paramTypeNames;
	}

	public Object[] getParamInputs() {
		return paramInputs;
	}

	public void setParamInputs(Object[] paramInputs) {
		this.paramInputs = paramInputs;
	}

}
