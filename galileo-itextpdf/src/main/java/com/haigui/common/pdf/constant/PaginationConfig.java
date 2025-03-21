package com.haigui.common.pdf.constant;

import java.io.Serializable;

/**
 * @Author: xuhui
 * @Description:
 * @Date: 2020/2/21 下午7:01
 */
public class PaginationConfig implements Serializable {

	private static final long serialVersionUID = 1267058977427915810L;

	/**
	 * 是否需要分页  y / n
	 */
	private String needPage = "n";

	/**
	 * 是否自动调整行高，解决最后一页只有落款的问题  y / n
	 */
	private String autoLineHeight= "n";


	/**
	 * 居中 center, 居左 left, 居右 right, 单页居左，双页居右 side
	 */
	private String alignMode = "center";

	/**
	 * 页码模式（0: %d, 1: %d/%d, 2: %d-%d, 3: 第%d页，共%d页, 4: 第%d页）
	 */
	private int showMode = 1;

	public PaginationConfig() {}


	public PaginationConfig(String alignMode, int showMode) {
		this.alignMode = alignMode;
		this.showMode = showMode;
		this.needPage = "y";
	}

	public String getAlignMode() {
		return alignMode;
	}

	public int getShowMode() {
		return showMode;
	}

	public String getNeedPage() {
		return needPage;
	}

	public void setNeedPage(String needPage) {
		this.needPage = needPage;
	}

	public void setAlignMode(String alignMode) {
		this.alignMode = alignMode;
	}

	public void setShowMode(int showMode) {
		this.showMode = showMode;
	}

	public static PaginationConfig defaultConfig() {
		return new PaginationConfig("center", PaginationMode.NUM_ZH_TOTAL.MODE);
	}


	public String getAutoLineHeight() {
		return autoLineHeight;
	}


	public void setAutoLineHeight(String autoLineHeight) {
		this.autoLineHeight = autoLineHeight;
	}

}
