package org.armada.galileo.mvc_plus.constant;


/**
 * session attributes中的常量定义
 * 
 * @author Wang Xiaobo
 * @date 2012-9-26
 */
public class SessionConstant {

	/**
	 * 上一次访问的url
	 */
	public static final String LAST_ACCESS_URL = "LAST_ACCESS_URL";

	/**
	 * 当前登录的管理员用户
	 */
	public static final String CURRENT_ADMIN = "CURRENT_ADMIN";

	/**
	 * 当前用户
	 */
	public static final String CURRENT_USER = "CURRENT_USER";

	/**
	 * 登录用户
	 */
	public static final String LOGIN_USER = "LOGIN_USER";

	/**
	 * session 中 token值
	 */
	public static final String FORM_TOKEN = "FORM_TOKEN";

	/**
	 * 验证码
	 */
	public static final String VALIDATE_CODE = "VALIDATE_CODE";

	/**
	 * 短信验证码
	 */
	public static final String Sms_validate_code = "Sms_validate_code";

	/**
	 * 接收短信验证码的手机号
	 */
	public static final String Sms_validate_code_mobile = "Sms_validate_code_mobile";

	/**
	 * 短信验证码过期时间
	 */
	public static final String Sms_validate_code_past_time = "Sms_validate_code_past_time";
}
