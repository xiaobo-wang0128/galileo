package com.haigui.common.pdf.util.help;


import org.armada.galileo.common.util.CommonUtil;

import java.util.ArrayList;
import java.util.List;

public enum PdfFont {

	// 微软雅黑
	MSYH("微软雅黑", "MSYH.TTF"),

	// 宋体
	SimSun("宋体", "STSONG.TTF"),

	// 黑体
	SimHei("黑体", "STXIHEI.TTF"),

//	// 仿宋
//	FangSong_GB2312("仿宋", "STFANGSO.TTF"),
//	// 仿宋gb2312
//	// FS_GB2312("仿宋_GB2312","FS_GB2312.TTF"),
//
//	FS_GB2312("仿宋_GB2312", "STFANGSO.TTF"),
//
//	// 楷体
//	KaiTi_GB2312("楷体", "STKAITI.TTF"),
//	// 隶书
//	LiShu_GB2312("隶书", "STLITI.TTF"),
//	// 华文中宋
//	STZhongsong("华文中宋", "STZHONGS.TTF"),


//	// 微软雅黑粗体
//	MSYHBD("微软雅黑粗体", "MSYHBD.TTF"),
//	// 新罗马
//	Times_New_Roman("Times New Roman", "TIMES.TTF")
	;

	private static List<PdfFont> fonts = new ArrayList<PdfFont>();

	static {
		fonts.addAll(CommonUtil.asList(PdfFont.values()));
	}

	public static PdfFont ofName(String name) {
		for (PdfFont font : fonts) {
			if (font.getName().equals(name)) {
				return font;
			}
		}
		return null;
	}

	/**
	 * 在引入本工具包时，系统会默认加载此枚举中定义的所有字体文件.<br/>
	 * 如果只希望加载部分字体，可调用此方法来指定字体，优化加载速度<br/>
	 * 比如智慧枫桥只用到了宋体，那么在这里指定宋体就可以了
	 *
	 * @param font
	 * @author Wang Xiaobo 2019年11月23日
	 */
	public static void registFonts(PdfFont... font) {
		if (font == null || font.length == 0) {
			return;
		}

		fonts.clear();
		for (PdfFont f : font) {
			fonts.add(f);
		}
	}

	public static List<PdfFont> getFonts() {
		return fonts;
	}

	/**
	 *
	 * @param name 字体名
	 * @param fontPath 文件名
	 */
	private PdfFont(String name, String fontPath) {
		this.name = name;
		this.fontPath = fontPath;
	}

	private String name;

	private String fontPath;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getFontPath() {
		return fontPath;
	}

	public void setFontPath(String fontPath) {
		this.fontPath = fontPath;
	}

}
