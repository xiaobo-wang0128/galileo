package org.armada.galileo.rainbow_gate.transfer.domain.data;

public class AppDeploy {

	/**
	 * 网关id
	 */
	private Long gateId;

	/**
	 * app 名称
	 */
	private String appName;

	/**
	 * app部署目录
	 */
	private String appDeployPath;

	/**
	 * 主机名
	 */
	private String hostName;

	/**
	 * 部署 url
	 */
	private String deployUrl;

	/**
	 * 进程id
	 */
	private String pid;

	/**
	 * 用户主目录
	 */
	private String userHome;

	/**
	 * 应用启动时间
	 */
	private String appStartTime;

	/**
	 * 系统类型
	 */
	private String osName;

	/**
	 * 指定路径
	 */
	private String specifyPath;

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public String getAppDeployPath() {
		return appDeployPath;
	}

	public void setAppDeployPath(String appDeployPath) {
		this.appDeployPath = appDeployPath;
	}

	public String getHostName() {
		return hostName;
	}

	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	public String getDeployUrl() {
		return deployUrl;
	}

	public void setDeployUrl(String deployUrl) {
		this.deployUrl = deployUrl;
	}

	public String getPid() {
		return pid;
	}

	public void setPid(String pid) {
		this.pid = pid;
	}

	public String getUserHome() {
		return userHome;
	}

	public void setUserHome(String userHome) {
		this.userHome = userHome;
	}

	public String getAppStartTime() {
		return appStartTime;
	}

	public void setAppStartTime(String appStartTime) {
		this.appStartTime = appStartTime;
	}

	public String getOsName() {
		return osName;
	}

	public void setOsName(String osName) {
		this.osName = osName;
	}

	public Long getGateId() {
		return gateId;
	}

	public void setGateId(Long gateId) {
		this.gateId = gateId;
	}

	public String getSpecifyPath() {
		return specifyPath;
	}

	public void setSpecifyPath(String specifyPath) {
		this.specifyPath = specifyPath;
	}
}
