package com.haigui.common.pdf.util.help;

public class DocPageParam {

	private String fontFamily;

	private String fontSize;

	private String contentLineHeight;

	private String titleLineHeight;

	// margin: 1em 0;
	/**
	 * 段落间距
	 */
	private String paragraphLineHeight;

	/**
	 * 是否替换数字和字母的字体
	 */
	private String replaceFontForAscii;

	public String getFontFamily() {
		return fontFamily;
	}

	public void setFontFamily(String fontFamily) {
		this.fontFamily = fontFamily;
	}

	public String getFontSize() {
		return fontSize;
	}

	public void setFontSize(String fontSize) {
		this.fontSize = fontSize;
	}

	public String getContentLineHeight() {
		return contentLineHeight;
	}

	public void setContentLineHeight(String contentLineHeight) {
		this.contentLineHeight = contentLineHeight;
	}

	public String getTitleLineHeight() {
		return titleLineHeight;
	}

	public void setTitleLineHeight(String titleLineHeight) {
		this.titleLineHeight = titleLineHeight;
	}

	public String getParagraphLineHeight() {
		return paragraphLineHeight;
	}

	public void setParagraphLineHeight(String paragraphLineHeight) {
		this.paragraphLineHeight = paragraphLineHeight;
	}

	public String getReplaceFontForAscii() {
		return replaceFontForAscii;
	}

	public void setReplaceFontForAscii(String replaceFontForAscii) {
		this.replaceFontForAscii = replaceFontForAscii;
	}
}
