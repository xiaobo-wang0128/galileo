package org.armada.spi.open.util;

/**
 * 请求参数封装
 *
 * @author xiaobo
 * @date 2021/4/23 10:07 上午
 */
public class HaiqSdkPostParam {

	private String appId;

	private String sign;

	private String dc;

	private byte[] bufs;

	public HaiqSdkPostParam() {
	}

	public HaiqSdkPostParam(String appId, String sign, String dc, byte[] bufs) {
		this.appId = appId;
		this.sign = sign;
		this.dc = dc;
		this.bufs = bufs;
	}

	public String getAppId() {
		return appId;
	}

	public void setAppId(String appId) {
		this.appId = appId;
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

}
