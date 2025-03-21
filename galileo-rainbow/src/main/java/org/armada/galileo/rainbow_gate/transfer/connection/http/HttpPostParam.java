package org.armada.galileo.rainbow_gate.transfer.connection.http;

public class HttpPostParam {

	private String appId;

	private String sign;

	private String dc;

	private String userId;

	private String token;

	private byte[] bufs;

	public HttpPostParam() {
	}

	public HttpPostParam(String appId, String sign, String dc, byte[] bufs) {
		this.appId = appId;
		this.sign = sign;
		this.dc = dc;
		this.bufs = bufs;
	}

	public String getSign() {
		return sign;
	}

	public void setSign(String sign) {
		this.sign = sign;
	}

	public byte[] getBufs() {
		return bufs;
	}

	public void setBufs(byte[] bufs) {
		this.bufs = bufs;
	}

	public String getDc() {
		return dc;
	}

	public void setDc(String dc) {
		this.dc = dc;
	}

	public String getAppId() {
		return appId;
	}

	public void setAppId(String appId) {
		this.appId = appId;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

}
