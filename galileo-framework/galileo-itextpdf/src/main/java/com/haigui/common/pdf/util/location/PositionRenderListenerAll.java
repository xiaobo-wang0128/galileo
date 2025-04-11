package com.haigui.common.pdf.util.location;

import java.util.ArrayList;
import java.util.List;

import com.itextpdf.awt.geom.Rectangle2D.Float;
import com.itextpdf.text.pdf.parser.ImageRenderInfo;
import com.itextpdf.text.pdf.parser.RenderListener;
import com.itextpdf.text.pdf.parser.TextRenderInfo;

/**
 * pdf渲染监听,当找到渲染的文本时，得到文本的坐标.获取关键字在页面出现的所有位置
 * 
 * @author xiaobowang
 *
 */
public class PositionRenderListenerAll implements RenderListener {

	private String findText;
	private float defaultH; /// 出现无法取到值的情况，默认为12
	private float fixHeight; // 可能出现无法完全覆盖的情况，提供修正的参数，默认为2

	/**
	 * 前段文本块的末尾包含关键字前缀
	 */
	private boolean hasPrefix;

	/**
	 * 持有的关键字前缀
	 */
	private String holderStr;

	private int indexCache;

	private Float boundCache;

	/**
	 * 可能匹配的region
	 */
	private ReplaceRegion holderRegion;

	/**
	 * 结果聚合
	 */
	private List<ReplaceRegion> resultList = new ArrayList<ReplaceRegion>();

	public PositionRenderListenerAll(String findText) {
		this.findText = findText;
		this.defaultH = 12;
		this.fixHeight = 2;
	}

	@Override
	public void beginTextBlock() {

	}

	@Override
	public void endTextBlock() {

	}

	@Override
	public void renderImage(ImageRenderInfo imageInfo) {
	}

	@Override
	public void renderText(TextRenderInfo textInfo) {
		if (shouldStop()) {
			return;
		}
		try {
			handleHeadText(textInfo);
			handleWholeText(textInfo);
			handleTailText(textInfo);
		}finally {
			boundCache = null;
		}

		//String text = textInfo.getText();
		//int tmpIndex = text.indexOf(findText);
		//
		//if (tmpIndex != -1) {
		//	Float bound = textInfo.getBaseline().getBoundingRectange();
		//	ReplaceRegion region = new ReplaceRegion(findText);
		//
		//	region.setH(bound.height == 0 ? defaultH : bound.height);
		//	region.setW(bound.width);
		//	region.setX(bound.x + tmpIndex * 12);
		//	region.setY(bound.y - this.fixHeight);
		//
		//	result.add(region);
		//}

	}

	/**
	 * 是否需要停止，留给子类扩展
	 * @return
	 */
	protected boolean shouldStop() {
		return false;
	}

	private ReplaceRegion extractRegion(int tmpIndex,TextRenderInfo textInfo) {
		setBoundCache(textInfo);
		Float bound = boundCache;
		ReplaceRegion region = new ReplaceRegion(findText);
		region.setH(bound.height == 0 ? defaultH : bound.height);
		region.setW(bound.width);
		region.setX(bound.x + tmpIndex * 12);
		region.setY(bound.y - this.fixHeight);
		return region;
	}

	private void setBoundCache(TextRenderInfo textInfo) {
		if (boundCache == null) {
			boundCache = textInfo.getBaseline().getBoundingRectange();
		}
	}

	private void handleWholeText(TextRenderInfo textInfo) {
		if (hasPrefix) {
			return;
		}
		String text = textInfo.getText();
		if ((text.length()-indexCache) < findText.length()) {
			indexCache = 0;
			return;
		}
		int fromIndex = indexCache;
		indexCache = 0;
		int index;
		while ((index = text.indexOf(findText, fromIndex)) > -1) {
			resultList.add(extractRegion(index,textInfo));
			fromIndex = index + findText.length();
			indexCache = fromIndex;
		}
	}

	private void handleTailText(TextRenderInfo textInfo) {
		if (hasPrefix) {
			return;
		}
		String text = textInfo.getText();
		int textLength = text.length();
		if (indexCache == textLength) {
			indexCache = 0;
			holderStr = null;
			holderRegion = null;
			return;
		}
		int limit = Math.min(textLength - indexCache, findText.length());
		while (limit > 0) {
			String substring = text.substring(textLength - limit);
			if (findText.startsWith(substring)) {
				hasPrefix = true;
				holderStr = substring;
				holderRegion = extractRegion(textLength - limit,textInfo);
				indexCache = 0;
				return;
			}
			limit--;
		}
		indexCache = 0;
		holderRegion = null;
		holderStr = null;

	}

	private void handleHeadText(TextRenderInfo textInfo) {
		if (!hasPrefix) {
			return;
		}
		String text = textInfo.getText();
		int length = holderStr.length();
		String remainingStr = findText.substring(length);
		if (text.length() < remainingStr.length()) {
			if (remainingStr.startsWith(text)) {
				holderStr += text;
				return;
			}
			holderStr = null;
			hasPrefix = false;
			holderRegion = null;
			return;
		}
		if (text.startsWith(remainingStr) && holderRegion != null) {
			resultList.add(holderRegion);
			indexCache = remainingStr.length();
		}
		holderStr = null;
		hasPrefix = false;
		holderRegion = null;
	}

	public List<ReplaceRegion> getResultList() {
		return resultList;
	}
}
