package org.armada.galileo.rainbow_gate.transfer.domain.protocol;

import java.io.Serializable;

import org.armada.galileo.common.util.CommonUtil;
import org.armada.galileo.rainbow_gate.transfer.domain.SftpAuth;
import org.armada.galileo.rainbow_gate.transfer.util.RainbowException;
import org.springframework.beans.BeanUtils;

/**
 * 远程通信参数对象
 * 
 * @author xiaobowang 2019年4月19日
 */
public class AppSecretObject implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * http sftp
	 */
	private String apiType;

	/**
	 * 每个租户的唯一标识
	 */
	private String appId;

	/*------ http 参数 ------*/
	/**
	 * http通信地址
	 */
	private String httpUrl;

	/**
	 * http通信密钥
	 */
	private String appSecret;

	/*------ sftp 参数 ------*/

	private SftpAuth sftpAuth;

	private AppSecretObject(String appId, String appSecret, String httpUrl) {
		this.appId = appId;
		this.appSecret = appSecret;
		this.httpUrl = httpUrl;
	}

	private AppSecretObject(String sftpHost, int sftpPort, String sftpUsername, String sftpPassword) {
		this.sftpAuth = new SftpAuth(sftpHost, sftpPort, sftpUsername, sftpPassword);
	}

	public AppSecretObject copy() {
		AppSecretObject r = new AppSecretObject();
		BeanUtils.copyProperties(this, r);
		return r;
	}

	public static AppSecretObject instanceHttp(String appId, String appSecret, String httpUrl) {

		if (CommonUtil.isEmpty(appId)) {
			throw new RainbowException("appId  不能为空");
		}
		if (CommonUtil.isEmpty(appSecret)) {
			throw new RainbowException("appSecret 密钥不能为空");
		}
		if (CommonUtil.isEmpty(httpUrl)) {
			throw new RainbowException("httpUrl 不能为空");
		}

		AppSecretObject appSecretObject = new AppSecretObject(appId, appSecret, httpUrl);
		appSecretObject.setApiType("http");
		return appSecretObject;
	}

	public static AppSecretObject instanceHttp(String apiType, String appId, String appSecret, String httpUrl) {

		if (CommonUtil.isEmpty(appId)) {
			throw new RainbowException("appId  不能为空");
		}
		if (CommonUtil.isEmpty(appSecret)) {
			throw new RainbowException("appSecret 密钥不能为空");
		}
		if (CommonUtil.isEmpty(httpUrl)) {
			throw new RainbowException("httpUrl 不能为空");
		}

		AppSecretObject appSecretObject = new AppSecretObject(appId, appSecret, httpUrl);
		appSecretObject.setApiType(apiType);

		return appSecretObject;
	}

	public static AppSecretObject instanceSftp(String ftpHost, int ftpPort, String ftpUsername, String ftpPassword) {
		if (ftpPort <= 0) {
			throw new RainbowException("sftp 端口号不正确");
		}
		if (CommonUtil.isEmpty(ftpUsername)) {
			throw new RainbowException("sftp 端口号不能为空");
		}
		if (CommonUtil.isEmpty(ftpPassword)) {
			throw new RainbowException("sftp 密码不能为空");
		}

		AppSecretObject appSecretObject = new AppSecretObject(ftpHost, ftpPort, ftpUsername, ftpPassword);
		appSecretObject.setApiType("sftp");
		return appSecretObject;
	}

	public static AppSecretObject instanceFtpOrSftp(String apiType, String ftpHost, int ftpPort, String ftpUsername, String ftpPassword) {
		if (ftpPort <= 0) {
			throw new RainbowException("sftp 端口号不正确");
		}
		if (CommonUtil.isEmpty(ftpUsername)) {
			throw new RainbowException("sftp 端口号不能为空");
		}
		if (CommonUtil.isEmpty(ftpPassword)) {
			throw new RainbowException("sftp 密码不能为空");
		}

		AppSecretObject appSecretObject = new AppSecretObject(ftpHost, ftpPort, ftpUsername, ftpPassword);
		appSecretObject.setApiType(apiType);
		return appSecretObject;
	}

	public static AppSecretObject instance(String apiType) {
		AppSecretObject appSecretObject = new AppSecretObject();
		appSecretObject.setApiType(apiType);
		return appSecretObject;
	}

	// public static AppSecretObject instanceFtp(String ftpHost, int ftpPort, String ftpUsername, String ftpPassword) {
	// AppSecretObject appSecretObject = new AppSecretObject(ftpHost, ftpPort, ftpUsername, ftpPassword);
	// appSecretObject.setType("sftp");
	// return appSecretObject;
	// }

	private AppSecretObject() {
	}

	public String getApiType() {
		return apiType;
	}

	public void setApiType(String apiType) {
		this.apiType = apiType;
	}

	public String getAppId() {
		return appId;
	}

	public void setAppId(String appId) {
		this.appId = appId;
	}

	public String getHttpUrl() {
		return httpUrl;
	}

	public void setHttpUrl(String httpUrl) {
		this.httpUrl = httpUrl;
	}

	public String getAppSecret() {
		return appSecret;
	}

	public void setAppSecret(String appSecret) {
		this.appSecret = appSecret;
	}

	public SftpAuth getSftpAuth() {
		return sftpAuth;
	}

	public void setSftpAuth(SftpAuth sftpAuth) {
		this.sftpAuth = sftpAuth;
	}

}
