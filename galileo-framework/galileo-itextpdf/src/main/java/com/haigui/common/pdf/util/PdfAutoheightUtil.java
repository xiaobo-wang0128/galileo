package com.haigui.common.pdf.util;

import java.io.File;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.armada.galileo.common.util.CommonUtil;
import org.slf4j.Logger;

import com.itextpdf.awt.geom.Rectangle2D;
import com.itextpdf.awt.geom.RectangularShape;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.ImageRenderInfo;
import com.itextpdf.text.pdf.parser.PdfReaderContentParser;
import com.itextpdf.text.pdf.parser.RenderListener;
import com.itextpdf.text.pdf.parser.TextRenderInfo;

public class PdfAutoheightUtil implements RenderListener {

	private static Logger log = org.slf4j.LoggerFactory.getLogger(PdfAutoheightUtil.class);

	// 用来存放文字的矩形
	private List<Rectangle2D.Float> rectText = new ArrayList<Rectangle2D.Float>();

	// 用来存放文字
	private List<String> textList = new ArrayList<String>();

	// 用来存放文字的y坐标
	private List<Float> listY = new ArrayList<Float>();

	// 用来存放每一行文字的坐标位置
	private List<Map<String, Rectangle2D.Float>> rows_text_rect = new ArrayList<>();

	// PDF文件的路径
	protected String filepath = null;

	public PdfAutoheightUtil() {
	}

	// step 2,遇到"BT"执行
	@Override
	public void beginTextBlock() {
		// TODO Auto-generated method stub
	}

	// step 3

	/**
	 * 文字主要处理方法
	 */
	@Override
	public void renderText(TextRenderInfo renderInfo) {
		// 获取文字的下面的矩形
		// Rectangle2D.Float rectBase = renderInfo.getBaseline().getBoundingRectange();

		String text = renderInfo.getText();
		if (text.length() > 0) {
			RectangularShape rectBase = renderInfo.getBaseline().getBoundingRectange();
			// 获取文字下面的矩形
			Rectangle2D.Float rectAscen = renderInfo.getAscentLine().getBoundingRectange();
			// 计算出文字的边框矩形
			float leftX = (float) rectBase.getMinX();
			float leftY = (float) rectBase.getMinY() - 1;
			float rightX = (float) rectAscen.getMaxX();
			float rightY = (float) rectAscen.getMaxY() - 1;

			Rectangle2D.Float rect = new Rectangle2D.Float(leftX, leftY, rightX - leftX, rightY - leftY);

			if (listY.contains(rect.y)) {
				int index = listY.indexOf(rect.y);
				float tempx = rect.x > rectText.get(index).x ? rectText.get(index).x : rect.x;
				rectText.set(index, new Rectangle2D.Float(tempx, rect.y, rect.width + rectText.get(index).width, rect.height));
				textList.set(index, textList.get(index) + text);
			}
			else {
				rectText.add(rect);
				textList.add(text);
				listY.add(rect.y);
			}

			Map<String, Rectangle2D.Float> map = new HashMap<>();
			map.put(text, rect);
			rows_text_rect.add(map);
		}
	}

	// step 4(最后执行的，只执行一次)，遇到“ET”执行
	@Override
	public void endTextBlock() {
		// TODO Auto-generated method stub
	}

	// step 1(图片处理方法)
	@Override
	public void renderImage(ImageRenderInfo renderInfo) {

	}

	private static Float MaxY = 733.57F;

	private static Float MinY = 89.57F;

	/**
	 * 最后一页行期望行数 >= 15， 每页标准行数 24行， 最后一页如果小于该行数，则进行调整
	 */
	private static Float TopY = MaxY - 15F * ((MaxY - MinY) / 24F);

	/**
	 * 最后一页如果只有2-3行，则期望通过 减小行高的方式来解决
	 */
	private static Float MaxTopY = MaxY - 2F * ((MaxY - MinY) / 24F);

	public static class PdfCheckResult {
		/**
		 * 是否需要优化
		 */
		private Boolean needOptimize;

		/**
		 * 优化方式
		 */
		private Way way;

		public static PdfCheckResult dontNeed() {
			PdfCheckResult r = new PdfCheckResult();
			r.needOptimize = false;
			return r;
		}

		public Boolean getNeedOptimize() {
			return needOptimize;
		}

		public void setNeedOptimize(Boolean needOptimize) {
			this.needOptimize = needOptimize;
		}

		public Way getWay() {
			return way;
		}

		public void setWay(Way way) {
			this.way = way;
		}

	}

	/**
	 * pdf 优化方式
	 *
	 * @author xiaobowang 2020年6月17日
	 */
	public static enum Way {

		/**
		 * 增加行高
		 */
		up,

		/**
		 * 缩小行高
		 */
		down
	}

	private static float MaxLineHeight = 2.3F;

	private static float MinLineHeight = 1.3F;

	private static float MiddleLineHeight = MinLineHeight + (MaxLineHeight - MinLineHeight) / 2;

	public static PdfCheckResult checkPdf(byte[] pdfs, float lineHeight) {

		try {
			PdfReader reader = new PdfReader(pdfs);

			// 新建一个PDF解析对象
			PdfReaderContentParser parser = new PdfReaderContentParser(reader);

			int pageCount = reader.getNumberOfPages();

			// pdf 只有一页
			if (pageCount <= 1) {
				return PdfCheckResult.dontNeed();
			}

			// 解析最后一页
			PdfAutoheightUtil listener = new PdfAutoheightUtil();

			// 解析PDF，并处理里面的文字
			parser.processContent(pageCount, listener);

			Float lastY = listener.listY.get(listener.listY.size() - 1);

			PdfCheckResult r = new PdfCheckResult();

			// 距离底部距离不足，则需要调整
			if (lastY > TopY) {
				r.setNeedOptimize(true);

				// 当前行高与 标准行高非常接近
				if (Math.abs(lineHeight - MiddleLineHeight) <= 0.1) {
					// 落款在 顶部
					if (lastY > MaxTopY) {
						r.setWay(Way.down);
					}
					else {
						r.setWay(Way.up);
					}
				}
				else {
					if (lineHeight < MiddleLineHeight) {
						r.setWay(Way.up);
					}
					else {
						r.setWay(Way.down);
					}
				}

			}
			else {
				r.setNeedOptimize(false);
			}

			return r;

		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}

		return null;
	}

	public static class DocumentResult {

		private byte[] pdfBytes;

		private String htmlContent;

		public DocumentResult(byte[] pdfBytes, String htmlContent) {
			this.pdfBytes = pdfBytes;
			this.htmlContent = htmlContent;
		}

		public byte[] getPdfBytes() {
			return pdfBytes;
		}

		public void setPdfBytes(byte[] pdfBytes) {
			this.pdfBytes = pdfBytes;
		}

		public String getHtmlContent() {
			return htmlContent;
		}

		public void setHtmlContent(String htmlContent) {
			this.htmlContent = htmlContent;
		}

	}

	public static DocumentResult optimizeLineHeight(byte[] pdfBytes, String htmlContent) {

		// 获取 html 的行高

		int fontSize = CommonUtil.getNumberFromStr(CommonUtil.substr(htmlContent, "font-size", ";"));

		int lineHeight = CommonUtil.getNumberFromStr(CommonUtil.substr(htmlContent, "line-height", ";"));

		if (fontSize <= 0 || lineHeight <= 0) {
			return null;
		}

		// 当前的行高
		float currentLineHeight = BigDecimal.valueOf(lineHeight).divide(BigDecimal.valueOf(fontSize), 2, RoundingMode.DOWN).floatValue();

		PdfCheckResult pr = checkPdf(pdfBytes, currentLineHeight);

		if (pr.getNeedOptimize()) {

			Way way = pr.getWay();

			String oldLineHeightStyle = "line-height:" + lineHeight + "pt;";

			if (htmlContent.indexOf(oldLineHeightStyle) == -1) {
				return new DocumentResult(pdfBytes, htmlContent);
			}

			int newLineHeight = new Integer(lineHeight);

			String htmlTmp = null;

			byte[] resultPdfs = null;

			int maxTryTimes = 30;
			int tryTimes = 0;
			while (true) {

				if (!pr.getNeedOptimize()) {
					break;
				}

				if (way.equals(Way.up)) {
					newLineHeight++;
				}
				else {
					newLineHeight--;
				}
				htmlTmp = CommonUtil.replaceAll(htmlContent, oldLineHeightStyle, "line-height:" + newLineHeight + "pt;");

				// 生成 新的 pdf
				log.info("优化pdf， 原 line-height: {}, 新 line-height:{}", oldLineHeightStyle, newLineHeight);
				resultPdfs = PdfGeneratorUtil.generatePdfByHtml(htmlTmp);

				pr = checkPdf(resultPdfs, newLineHeight);

				tryTimes++;

				// 超过优化次数 直接退出
				if (tryTimes >= maxTryTimes) {
					return new DocumentResult(pdfBytes, htmlContent);
				}
			}

			if (resultPdfs != null) {
				log.info("优化pdf 完成");
				log.info("");
				return new DocumentResult(resultPdfs, htmlTmp);
			}

		}

		return new DocumentResult(pdfBytes, htmlContent);
	}

//	public static void main(String[] args) throws Exception {
//
//		String folder = "/Users/xiaobowang/Downloads/doc_test";
//
//		String pdfFolder1 = "/Users/xiaobowang/Downloads/pdf_1";
//
//		String pdfFolder2 = "/Users/xiaobowang/Downloads/pdf_2";
//
//		File[] fs = new File(folder).listFiles();
//
//		for (File file : fs) {
//			if (file.getName().endsWith(".html")) {
//
//				byte[] bytes = CommonUtil.readFileFromLocal(file.getAbsolutePath());
//				String content = new String(bytes, "utf-8");
//
//				byte[] pdfs = PdfGeneratorUtil.generatePdfByHtml(content);
//				System.out.println();
//				log.info("[pdf optimize] name: " + file.getName());
//
//				String pdfName = pdfFolder1 + "/" + file.getName().substring(0, file.getName().lastIndexOf(".")) + ".pdf";
//				FileOutputStream fos = new FileOutputStream(pdfName);
//				fos.write(pdfs);
//				fos.close();
//
//				DocumentResult docResult = optimizeLineHeight(pdfs, content);
//
//				pdfs = docResult.getPdfBytes();
//				pdfName = pdfFolder2 + "/" + file.getName().substring(0, file.getName().lastIndexOf(".")) + ".pdf";
//				fos = new FileOutputStream(pdfName);
//				fos.write(pdfs);
//				fos.close();
//
//				pdfName = pdfFolder2 + "/" + file.getName().substring(0, file.getName().lastIndexOf(".")) + ".docx";
//				fos = new FileOutputStream(pdfName);
//				byte[] docxBytes = OfficeWordUtil.generateWordByHtml(docResult.getHtmlContent());
//				fos.write(docxBytes);
//				fos.close();
//
//			}
//		}
//	}

	public static void main22(String[] args) {
		try {
			PdfReader reader = new PdfReader("/Users/xiaobowang/Downloads/doc_test/1206371.pdf");

			// 新建一个PDF解析对象
			PdfReaderContentParser parser = new PdfReaderContentParser(reader);
			for (int i = reader.getNumberOfPages(); i <= reader.getNumberOfPages(); i++) {
				// 新建一个ImageRenderListener对象，该对象实现了RenderListener接口，作为处理PDF的主要类
				PdfAutoheightUtil listener = new PdfAutoheightUtil();
				// 解析PDF，并处理里面的文字
				parser.processContent(i, listener);
				// 获取文字的矩形边框
				List<Map<String, Rectangle2D.Float>> list_text = listener.rows_text_rect;

				for (int k = 0; k < list_text.size(); k++) {
					Map<String, Rectangle2D.Float> map = list_text.get(k);
					for (Map.Entry<String, Rectangle2D.Float> entry : map.entrySet()) {
						System.out.println(entry.getKey() + "----" + entry.getValue());
					}
				}

				System.out.println(listener.listY.get(listener.listY.size() - 1));
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
