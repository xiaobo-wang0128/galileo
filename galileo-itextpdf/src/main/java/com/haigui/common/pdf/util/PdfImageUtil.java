package com.haigui.common.pdf.util;

import com.haigui.common.pdf.util.location.PositionRenderListenerAll;
import com.haigui.common.pdf.util.location.ReplaceRegion;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.parser.PdfReaderContentParser;
import org.apache.commons.lang3.StringUtils;
import org.armada.galileo.common.util.CommonUtil;
import org.armada.galileo.exception.BizException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class PdfImageUtil {

	private static Logger log = LoggerFactory.getLogger(PdfImageUtil.class);


	public static byte[] writeImageToPdf(byte[] pdfBytes, List<FloatImage> params) {
		return writeImageToPdf(pdfBytes, params, null);
	}

	public static byte[] writeImageToPdf(byte[] pdfBytes, List<FloatImage> params, FilterInfo filterInfo) {
		try {

			long l1 = System.currentTimeMillis();

			if (pdfBytes == null || pdfBytes.length == 0) {
				throw new BizException("pdf数据不能为空");
			}
			if (params == null || params.isEmpty()) {
				throw new BizException("图片参数信息不能为空");
			}

			Collections.sort(params, new Comparator<FloatImage>() {
				public int compare(FloatImage o1, FloatImage o2) {
					if (StringUtils.isAnyBlank(o1.getKeyword(), o2.getKeyword())) {
						if (o1.getOffsetY() == o2.getOffsetY()) {
							return o1.getOffsetX() < o2.getOffsetX() ? -1 : 1;
						}
						return o1.getOffsetY() < o2.getOffsetY() ? -1 : 1;
					}
					int main = o1.getKeyword().compareTo(o2.getKeyword());
					if (main != 0) {
						return main;
					}

					return o1.getKeyworkIndex() < o2.getKeyworkIndex() ? -1 : 1;
				}
			});

			PdfReader pdfReader = new PdfReader(pdfBytes);

			PdfReaderContentParser parser = new PdfReaderContentParser(pdfReader);

			ByteArrayOutputStream output = new ByteArrayOutputStream();

			PdfStamper stamper = new PdfStamper(pdfReader, output);

			// 关键字当前出现的次数
			Map<String, Integer> keywordIndexMap = new HashMap<String, Integer>();

			int pages = pdfReader.getNumberOfPages();

			// 缓存页码-关键字， 关键字的位置信息列表
			Map<String, List<ReplaceRegion>> regionCache = new HashMap<String, List<ReplaceRegion>>();

			for (int i = 0; i < pages; i++) {
				int page = i + 1;

				if (filterInfo != null && (CommonUtil.isNotEmpty(filterInfo.getPageNos()) || CommonUtil.isNotEmpty(filterInfo.getKeywords()))) {
					boolean isPageNoPass = CommonUtil.isEmpty(filterInfo.getPageNos()) || filterInfo.getPageNos().contains(page) ||
							(filterInfo.getPageNos().contains(-1) && page == pages);

					boolean isKeywordPass = CommonUtil.isEmpty(filterInfo.getKeywords());
					if (!isKeywordPass) {
						for (String keyword : filterInfo.getKeywords()) {
							String filterKeyword = i + keyword;
							List<ReplaceRegion> replaceRegions = regionCache.get(filterKeyword);
							if (replaceRegions == null) {
								PositionRenderListenerAll listener = new PositionRenderListenerAll(keyword);
								parser.processContent(page, listener);
								replaceRegions = listener.getResultList();
								regionCache.put(filterKeyword, replaceRegions);
							}
							if (isKeywordPass = CommonUtil.isNotEmpty(replaceRegions)) {
								break;
							}
						}
					}
					if (isPageNoPass && isKeywordPass) {
						continue;
					}
				}

				// 扫描关键字位置
				main: for (Iterator<FloatImage> it = params.iterator(); it.hasNext();) {

					FloatImage param = it.next();

					if(StringUtils.isBlank(param.getKeyword())) {
						com.itextpdf.text.Image image = com.itextpdf.text.Image.getInstance(param.getImageBytes());
						PdfContentByte under = stamper.getOverContent(page);
						image.scaleToFit(param.getImageWidth(), param.getImageHeight());
						image.setAbsolutePosition(param.getOffsetX(), param.getOffsetY());
						under.addImage(image);
					} else {

						String pageKeyword = i + param.getKeyword();

						List<ReplaceRegion> replaceRegions = regionCache.get(pageKeyword);

						if (replaceRegions == null) {

							PositionRenderListenerAll listener = new PositionRenderListenerAll(param.getKeyword());

							parser.processContent(page, listener);

							replaceRegions = listener.getResultList();

							regionCache.put(pageKeyword, replaceRegions);
						}

						//
						if (replaceRegions != null && replaceRegions.size() > 0) {

							Integer startIndex = keywordIndexMap.get(param.getKeyword());
							if (startIndex == null) {
								startIndex = 0;
							}

							for (Iterator<ReplaceRegion> it2 = replaceRegions.iterator(); it2.hasNext(); ) {

								ReplaceRegion regin = it2.next();

								startIndex++;

								keywordIndexMap.put(param.getKeyword(), startIndex);

								// 指定了关键字出现的位置
								if (param.getKeyworkIndex() > 0) {

									// 关键字出现的位置 正好匹配
									if (startIndex == param.getKeyworkIndex()) {

										com.itextpdf.text.Image image = com.itextpdf.text.Image.getInstance(param.getImageBytes());
										PdfContentByte under = stamper.getOverContent(page);
										image.scaleToFit(param.getImageWidth(), param.getImageHeight());
										image.setAbsolutePosition(regin.getX() + param.getOffsetX(), regin.getY() + param.getOffsetY());
										under.addImage(image);

										it2.remove();
										continue main;
									}

								}
								// 未指
								else {

									com.itextpdf.text.Image image = com.itextpdf.text.Image.getInstance(param.getImageBytes());
									PdfContentByte under = stamper.getOverContent(page);
									image.scaleToFit(param.getImageWidth(), param.getImageHeight());
									image.setAbsolutePosition(regin.getX() + param.getOffsetX(), regin.getY() + param.getOffsetY());
									under.addImage(image);

								}

							}

						}
					}
				}
			}

			output.flush();
			stamper.close();

			long l2 = System.currentTimeMillis();
			byte[] out = output.toByteArray();

			log.info("[pdf] add image to pdf cost time: " + (l2 - l1) + "ms");
			return out;

		} catch (Exception e) {

			log.info(e.getMessage(), e);

			throw new BizException(e);
		}
	}

	public static class FloatImage {

		/**
		 * 图片byte
		 */
		private byte[] imageBytes;

		/**
		 * pdf上展示时的图片宽度
		 */
		private float imageWidth;

		/**
		 * pdf上展示时的图片高度
		 */
		private float imageHeight;

		/**
		 * 关键字（图片出现在pdf上的位置，通过关键字定位）
		 */
		private String keyword;

		/**
		 * 关键字出现的次数， 从 1 开始，<= 0 表示所有 keyword 都需要p图
		 */
		private int keyworkIndex;

		/**
		 * 偏移量 x
		 */
		private int offsetX = 0;

		/**
		 * 偏移量 y
		 */
		private int offsetY = 0;

		public FloatImage() {
		}

		public FloatImage(byte[] imageBytes, float imageWidth, float imageHeight, String keyword, int offsetX, int offsetY) {
			this.imageBytes = imageBytes;
			this.imageWidth = imageWidth;
			this.imageHeight = imageHeight;
			this.keyword = keyword;
			this.offsetX = offsetX - (int)(imageWidth / 2);
			this.offsetY = offsetY - (int)(imageHeight / 2);
		}

		/**
		 * p图对象参数
		 *
		 * @param imageBytes 图片字节码
		 * @param imageWidth 显示宽度
		 * @param imageHeight 显示高度
		 * @param keyword 定位的关键字
		 * @param keyworkIndex 在第几个关键字上p图， 小于0表示所有
		 * @param offsetX 偏移量 X
		 * @param offsetY 偏移量 Y
		 */
		public FloatImage(byte[] imageBytes, float imageWidth, float imageHeight, String keyword, int keyworkIndex, int offsetX, int offsetY) {
			super();
			this.imageBytes = imageBytes;
			this.imageWidth = imageWidth;
			this.imageHeight = imageHeight;
			this.keyword = keyword;
			this.keyworkIndex = keyworkIndex;
			this.offsetX = offsetX;
			this.offsetY = offsetY;
		}

		public byte[] getImageBytes() {
			return imageBytes;
		}

		public void setImageBytes(byte[] imageBytes) {
			this.imageBytes = imageBytes;
		}

		public float getImageWidth() {
			return imageWidth;
		}

		public void setImageWidth(float imageWidth) {
			this.imageWidth = imageWidth;
		}

		public float getImageHeight() {
			return imageHeight;
		}

		public void setImageHeight(float imageHeight) {
			this.imageHeight = imageHeight;
		}

		public String getKeyword() {
			return keyword;
		}

		public void setKeyword(String keyword) {
			this.keyword = keyword;
		}

		public int getKeyworkIndex() {
			return keyworkIndex;
		}

		public void setKeyworkIndex(int keyworkIndex) {
			this.keyworkIndex = keyworkIndex;
		}

		public int getOffsetX() {
			return offsetX;
		}

		public void setOffsetX(int offsetX) {
			this.offsetX = offsetX;
		}

		public int getOffsetY() {
			return offsetY;
		}

		public void setOffsetY(int offsetY) {
			this.offsetY = offsetY;
		}

	}

	public static class FilterInfo {

		/**
		 * 需要过滤的页码，-1表示最后一页，从1开始
		 */
		private List<Integer> pageNos;

		/**
		 * 关键字（通过页面是否包含关键字 去过滤）
		 */
		private List<String> keywords;

		public FilterInfo() {
		}

		public FilterInfo(List<Integer> pageNos, List<String> keywords) {
			this.pageNos = pageNos;
			this.keywords = keywords;
		}

		public List<Integer> getPageNos() {
			return pageNos;
		}

		public void setPageNos(List<Integer> pageNos) {
			this.pageNos = pageNos;
		}

		public List<String> getKeywords() {
			return keywords;
		}

		public void setKeywords(List<String> keywords) {
			this.keywords = keywords;
		}
	}
}
