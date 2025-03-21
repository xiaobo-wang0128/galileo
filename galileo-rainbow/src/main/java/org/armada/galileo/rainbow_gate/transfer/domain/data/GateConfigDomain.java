package org.armada.galileo.rainbow_gate.transfer.domain.data;

import java.util.Date;

/**
 * 网关配置
 * 
 * @author xiaobowang
 *
 */
public class GateConfigDomain {

	private Long id;

	private String securityId;

	private Date gmtCreate;

	private String creator;

	private Date gmtModified;

	private String modifier;

	private String isDeleted;

	/**
	 * 对接名称
	 */
	private String name;

	/**
	 * 状态 enable disable
	 */
	private String status;

	/**
	 * 法院id集合 json
	 */
	private String courtIds;

	/**
	 * 法院名称集合 json
	 */
	private String courtNames;

	/**
	 * 对接类型 share_sftp 单台共享sftp、 single_ftp 单向ftp、single_sftp 单向sftp、http 直连模式、liyue 腾讯里约、two_channel_ftp 双向ftp
	 */
	private String gateType;

	/**
	 * 远程接口地址
	 */
	private String httpApiAddress;

	/**
	 * 远程接口appId
	 */
	private String httpAppId;

	/**
	 * 远程接口appSecret
	 */
	private String httpAppSecret;

	/**
	 * ftp远程url，格式 ip:端口号
	 */
	private String ftpRemoteUrl;

	/**
	 * ftp账户名
	 */
	private String ftpUserName;

	/**
	 * ftp密码
	 */
	private String ftpUserPwd;

	/**
	 * ftp远程url，格式 ip:端口号（双向写）
	 */
	private String ftpRemoteUrlWrite;

	/**
	 * ftp账户名（双向写）
	 */
	private String ftpUserNameWrite;

	/**
	 * ftp密码（双向写）
	 */
	private String ftpUserPwdWrite;

	/**
	 * ftp远程url，格式 ip:端口号（双向读）
	 */
	private String ftpRemoteUrlRead;

	/**
	 * ftp账户名（双向读）
	 */
	private String ftpUserNameRead;

	/**
	 * ftp密码（双向读）
	 */
	private String ftpUserPwdRead;

	/**
	 * 是否开启临时关键字过滤
	 */
	private String ignoreKeyOpen;

	/**
	 * 关键字
	 */
	private String ignoreKeyWord;

	/**
	 * 关键字位置 front 前缀 end 后缀
	 */
	private String ignoreKeyPosition;

	/**
	 * 是否开启自动审核开关
	 */
	private String isAutoAudit;

	/**
	 * 获取对接名称
	 *
	 * @return name - 对接名称
	 */
	public String getName() {
		return name;
	}

	/**
	 * 设置对接名称
	 *
	 * @param name 对接名称
	 */
	public void setName(String name) {
		this.name = name == null ? null : name.trim();
	}

	/**
	 * 获取状态 enable disable
	 *
	 * @return status - 状态 enable disable
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * 设置状态 enable disable
	 *
	 * @param status 状态 enable disable
	 */
	public void setStatus(String status) {
		this.status = status == null ? null : status.trim();
	}

	/**
	 * 获取法院id集合 json
	 *
	 * @return court_ids - 法院id集合 json
	 */
	public String getCourtIds() {
		return courtIds;
	}

	/**
	 * 设置法院id集合 json
	 *
	 * @param courtIds 法院id集合 json
	 */
	public void setCourtIds(String courtIds) {
		this.courtIds = courtIds == null ? null : courtIds.trim();
	}

	/**
	 * 获取法院名称集合 json
	 *
	 * @return court_names - 法院名称集合 json
	 */
	public String getCourtNames() {
		return courtNames;
	}

	/**
	 * 设置法院名称集合 json
	 *
	 * @param courtNames 法院名称集合 json
	 */
	public void setCourtNames(String courtNames) {
		this.courtNames = courtNames == null ? null : courtNames.trim();
	}

	/**
	 * 获取对接类型 share_sftp 单台共享sftp、 single_ftp 单向ftp、single_sftp 单向sftp、http 直连模式、liyue 腾讯里约、two_channel_ftp 双向ftp
	 *
	 * @return gate_type - 对接类型 share_sftp 单台共享sftp、 single_ftp 单向ftp、single_sftp 单向sftp、http 直连模式、liyue 腾讯里约、two_channel_ftp 双向ftp
	 */
	public String getGateType() {
		return gateType;
	}

	/**
	 * 设置对接类型 share_sftp 单台共享sftp、 single_ftp 单向ftp、single_sftp 单向sftp、http 直连模式、liyue 腾讯里约、two_channel_ftp 双向ftp
	 *
	 * @param gateType 对接类型 share_sftp 单台共享sftp、 single_ftp 单向ftp、single_sftp 单向sftp、http 直连模式、liyue 腾讯里约、two_channel_ftp 双向ftp
	 */
	public void setGateType(String gateType) {
		this.gateType = gateType == null ? null : gateType.trim();
	}

	/**
	 * 获取远程接口地址
	 *
	 * @return http_api_address - 远程接口地址
	 */
	public String getHttpApiAddress() {
		return httpApiAddress;
	}

	/**
	 * 设置远程接口地址
	 *
	 * @param httpApiAddress 远程接口地址
	 */
	public void setHttpApiAddress(String httpApiAddress) {
		this.httpApiAddress = httpApiAddress == null ? null : httpApiAddress.trim();
	}

	/**
	 * 获取远程接口appId
	 *
	 * @return http_app_id - 远程接口appId
	 */
	public String getHttpAppId() {
		return httpAppId;
	}

	/**
	 * 设置远程接口appId
	 *
	 * @param httpAppId 远程接口appId
	 */
	public void setHttpAppId(String httpAppId) {
		this.httpAppId = httpAppId == null ? null : httpAppId.trim();
	}

	/**
	 * 获取远程接口appSecret
	 *
	 * @return http_app_secret - 远程接口appSecret
	 */
	public String getHttpAppSecret() {
		return httpAppSecret;
	}

	/**
	 * 设置远程接口appSecret
	 *
	 * @param httpAppSecret 远程接口appSecret
	 */
	public void setHttpAppSecret(String httpAppSecret) {
		this.httpAppSecret = httpAppSecret == null ? null : httpAppSecret.trim();
	}

	/**
	 * 获取ftp远程url，格式 ip:端口号
	 *
	 * @return ftp_remote_url - ftp远程url，格式 ip:端口号
	 */
	public String getFtpRemoteUrl() {
		return ftpRemoteUrl;
	}

	/**
	 * 设置ftp远程url，格式 ip:端口号
	 *
	 * @param ftpRemoteUrl ftp远程url，格式 ip:端口号
	 */
	public void setFtpRemoteUrl(String ftpRemoteUrl) {
		this.ftpRemoteUrl = ftpRemoteUrl == null ? null : ftpRemoteUrl.trim();
	}

	/**
	 * 获取ftp账户名
	 *
	 * @return ftp_user_name - ftp账户名
	 */
	public String getFtpUserName() {
		return ftpUserName;
	}

	/**
	 * 设置ftp账户名
	 *
	 * @param ftpUserName ftp账户名
	 */
	public void setFtpUserName(String ftpUserName) {
		this.ftpUserName = ftpUserName == null ? null : ftpUserName.trim();
	}

	/**
	 * 获取ftp密码
	 *
	 * @return ftp_user_pwd - ftp密码
	 */
	public String getFtpUserPwd() {
		return ftpUserPwd;
	}

	/**
	 * 设置ftp密码
	 *
	 * @param ftpUserPwd ftp密码
	 */
	public void setFtpUserPwd(String ftpUserPwd) {
		this.ftpUserPwd = ftpUserPwd == null ? null : ftpUserPwd.trim();
	}

	/**
	 * 获取ftp远程url，格式 ip:端口号（双向写）
	 *
	 * @return ftp_remote_url_write - ftp远程url，格式 ip:端口号（双向写）
	 */
	public String getFtpRemoteUrlWrite() {
		return ftpRemoteUrlWrite;
	}

	/**
	 * 设置ftp远程url，格式 ip:端口号（双向写）
	 *
	 * @param ftpRemoteUrlWrite ftp远程url，格式 ip:端口号（双向写）
	 */
	public void setFtpRemoteUrlWrite(String ftpRemoteUrlWrite) {
		this.ftpRemoteUrlWrite = ftpRemoteUrlWrite == null ? null : ftpRemoteUrlWrite.trim();
	}

	/**
	 * 获取ftp账户名（双向写）
	 *
	 * @return ftp_user_name_write - ftp账户名（双向写）
	 */
	public String getFtpUserNameWrite() {
		return ftpUserNameWrite;
	}

	/**
	 * 设置ftp账户名（双向写）
	 *
	 * @param ftpUserNameWrite ftp账户名（双向写）
	 */
	public void setFtpUserNameWrite(String ftpUserNameWrite) {
		this.ftpUserNameWrite = ftpUserNameWrite == null ? null : ftpUserNameWrite.trim();
	}

	/**
	 * 获取ftp密码（双向写）
	 *
	 * @return ftp_user_pwd_write - ftp密码（双向写）
	 */
	public String getFtpUserPwdWrite() {
		return ftpUserPwdWrite;
	}

	/**
	 * 设置ftp密码（双向写）
	 *
	 * @param ftpUserPwdWrite ftp密码（双向写）
	 */
	public void setFtpUserPwdWrite(String ftpUserPwdWrite) {
		this.ftpUserPwdWrite = ftpUserPwdWrite == null ? null : ftpUserPwdWrite.trim();
	}

	/**
	 * 获取ftp远程url，格式 ip:端口号（双向读）
	 *
	 * @return ftp_remote_url_read - ftp远程url，格式 ip:端口号（双向读）
	 */
	public String getFtpRemoteUrlRead() {
		return ftpRemoteUrlRead;
	}

	/**
	 * 设置ftp远程url，格式 ip:端口号（双向读）
	 *
	 * @param ftpRemoteUrlRead ftp远程url，格式 ip:端口号（双向读）
	 */
	public void setFtpRemoteUrlRead(String ftpRemoteUrlRead) {
		this.ftpRemoteUrlRead = ftpRemoteUrlRead == null ? null : ftpRemoteUrlRead.trim();
	}

	/**
	 * 获取ftp账户名（双向读）
	 *
	 * @return ftp_user_name_read - ftp账户名（双向读）
	 */
	public String getFtpUserNameRead() {
		return ftpUserNameRead;
	}

	/**
	 * 设置ftp账户名（双向读）
	 *
	 * @param ftpUserNameRead ftp账户名（双向读）
	 */
	public void setFtpUserNameRead(String ftpUserNameRead) {
		this.ftpUserNameRead = ftpUserNameRead == null ? null : ftpUserNameRead.trim();
	}

	/**
	 * 获取ftp密码（双向读）
	 *
	 * @return ftp_user_pwd_read - ftp密码（双向读）
	 */
	public String getFtpUserPwdRead() {
		return ftpUserPwdRead;
	}

	/**
	 * 设置ftp密码（双向读）
	 *
	 * @param ftpUserPwdRead ftp密码（双向读）
	 */
	public void setFtpUserPwdRead(String ftpUserPwdRead) {
		this.ftpUserPwdRead = ftpUserPwdRead == null ? null : ftpUserPwdRead.trim();
	}

	/**
	 * 获取是否开启临时关键字过滤
	 *
	 * @return ignore_key_open - 是否开启临时关键字过滤
	 */
	public String getIgnoreKeyOpen() {
		return ignoreKeyOpen;
	}

	/**
	 * 设置是否开启临时关键字过滤
	 *
	 * @param ignoreKeyOpen 是否开启临时关键字过滤
	 */
	public void setIgnoreKeyOpen(String ignoreKeyOpen) {
		this.ignoreKeyOpen = ignoreKeyOpen == null ? null : ignoreKeyOpen.trim();
	}

	/**
	 * 获取关键字
	 *
	 * @return ignore_key_word - 关键字
	 */
	public String getIgnoreKeyWord() {
		return ignoreKeyWord;
	}

	/**
	 * 设置关键字
	 *
	 * @param ignoreKeyWord 关键字
	 */
	public void setIgnoreKeyWord(String ignoreKeyWord) {
		this.ignoreKeyWord = ignoreKeyWord == null ? null : ignoreKeyWord.trim();
	}

	/**
	 * 获取关键字位置 front前缀 end 后缀
	 *
	 * @return ignore_key_position - 关键字位置 front前缀 end 后缀
	 */
	public String getIgnoreKeyPosition() {
		return ignoreKeyPosition;
	}

	/**
	 * 设置关键字位置 front前缀 end 后缀
	 *
	 * @param ignoreKeyPosition 关键字位置 front前缀 end 后缀
	 */
	public void setIgnoreKeyPosition(String ignoreKeyPosition) {
		this.ignoreKeyPosition = ignoreKeyPosition == null ? null : ignoreKeyPosition.trim();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getSecurityId() {
		return securityId;
	}

	public void setSecurityId(String securityId) {
		this.securityId = securityId;
	}

	public Date getGmtCreate() {
		return gmtCreate;
	}

	public void setGmtCreate(Date gmtCreate) {
		this.gmtCreate = gmtCreate;
	}

	public String getCreator() {
		return creator;
	}

	public void setCreator(String creator) {
		this.creator = creator;
	}

	public Date getGmtModified() {
		return gmtModified;
	}

	public void setGmtModified(Date gmtModified) {
		this.gmtModified = gmtModified;
	}

	public String getModifier() {
		return modifier;
	}

	public void setModifier(String modifier) {
		this.modifier = modifier;
	}

	public String getIsDeleted() {
		return isDeleted;
	}

	public void setIsDeleted(String isDeleted) {
		this.isDeleted = isDeleted;
	}

	public String getIsAutoAudit() {
		return isAutoAudit;
	}

	public void setIsAutoAudit(String isAutoAudit) {
		this.isAutoAudit = isAutoAudit;
	}
}
