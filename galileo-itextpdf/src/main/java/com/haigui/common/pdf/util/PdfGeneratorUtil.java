package com.haigui.common.pdf.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import com.haigui.common.pdf.constant.PaginationConfig;
import com.haigui.common.pdf.util.PdfAutoheightUtil.DocumentResult;
import com.haigui.common.pdf.util.PdfImageUtil.FloatImage;
import com.haigui.common.pdf.util.help.PdfFont;
import com.haigui.common.pdf.util.location.PositionRenderListener;
import com.haigui.common.pdf.util.location.ReplaceRegion;
import com.haigui.common.util.DocumentConfig;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.RectangleReadOnly;
import com.itextpdf.text.log.Logger;
import com.itextpdf.text.log.LoggerFactory;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.parser.PdfReaderContentParser;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;
import com.itextpdf.tool.xml.Pipeline;
import com.itextpdf.tool.xml.Tag;
import com.itextpdf.tool.xml.WorkerContext;
import com.itextpdf.tool.xml.XMLWorker;
import com.itextpdf.tool.xml.XMLWorkerFontProvider;
import com.itextpdf.tool.xml.css.StyleAttrCSSResolver;
import com.itextpdf.tool.xml.html.CssAppliers;
import com.itextpdf.tool.xml.html.CssAppliersImpl;
import com.itextpdf.tool.xml.html.HTML;
import com.itextpdf.tool.xml.html.TagProcessorFactory;
import com.itextpdf.tool.xml.html.Tags;
import com.itextpdf.tool.xml.parser.XMLParser;
import com.itextpdf.tool.xml.pipeline.css.CSSResolver;
import com.itextpdf.tool.xml.pipeline.css.CssResolverPipeline;
import com.itextpdf.tool.xml.pipeline.end.PdfWriterPipeline;
import com.itextpdf.tool.xml.pipeline.html.HtmlPipeline;
import com.itextpdf.tool.xml.pipeline.html.HtmlPipelineContext;
import lombok.extern.slf4j.Slf4j;
import org.armada.galileo.common.util.CommonUtil;
import org.armada.galileo.common.util.JsonUtil;
import org.armada.galileo.exception.BizException;

@Slf4j
public class PdfGeneratorUtil {

	// public static void main(String[] args) throws IOException {
	// String path = "/Users/xuhui/Desktop/error_doc.html";
	// byte[] bytes = Files.readAllBytes(Paths.get(path));
	// String content = new String(bytes, "utf-8");
	// byte[] images = null;
	// byte[] bytes1 = PdfGeneratorUtil.generatePdfByHtml(content, images);
	// Files.write(Paths.get("/Users/xuhui/Desktop/error_doc.pdf"), bytes1);
	// }

	/**
	 * 字体文件256校验码
	 */
	private static Map<String, String> fontSha256 = new LinkedHashMap<String, String>();

	static {
		fontSha256.put("SimSun", "c5cc2ed5e2c0e6385013fe82d950eee6960d805bd602b86c53ff454783f382c4");
		fontSha256.put("XiaoSimSun", "2322dbfb4fe3b51e5f530c0ee668a7d83df20c0fd8b1aa113a0a9e7ee45d45f3");
		fontSha256.put("SimHei", "f4ccbf1caf92f38ee8f8d63edf7cf78856fdf964982d719632ef3f6960ab9fea");
		fontSha256.put("FangSong_GB2312", "e6326459e8e60e436c7d60e34d273bda3ba4eea2d2a5b309ff8f1b73200f2e38");
		fontSha256.put("FS_GB2312", "e6326459e8e60e436c7d60e34d273bda3ba4eea2d2a5b309ff8f1b73200f2e38");
		fontSha256.put("KaiTi_GB2312", "a29c99c161fc43ce6aba2d7c152065359c2cb3019be4ae6248171178cb7d04d5");
		fontSha256.put("LiShu_GB2312", "cf135c073d731ef7e7ae86e715b7efccad41fe915d620bff393a07d70a190865");
		fontSha256.put("STZhongsong", "e822a3f2859a6cc982f04c6ad1eabc18371ce26d6874b710188a30c35dced0f1");
		fontSha256.put("MSYH", "1547ada4f8a1d8f12f154d221009b90e9389ea77f3911c8fab6494e533cf0ef2");
		fontSha256.put("MSYHBD", "902de9fa292978d04e5052b5005bcc445ebb0907eb83245386e559f3aba732cd");
		fontSha256.put("Times_New_Roman", "2cff2a03d8034801979dd6d16f09b9a825c3d710fcf068f2ebfbf0e1425c87cf");
	}


	/**
	 * 默认签章替换符
	 */
	public static String SignaturePosition = "!S!";

	// private static byte[] imgBuff = CommonUtil.readFileToBuffer("image/haigui_150_150.png");

	static Map<String, BaseFont> pdfFontMap = new HashMap<String, BaseFont>();

	static BaseFont defaultFont = null;

	public static float rate = 0.03525F;
	public static float left = 2.55F / rate;
	public static float right = 2.55F / rate;

	public static float top = 2.8F / rate;
	public static float bottom = 2.8F / rate;

	private static AtomicBoolean hasInit = new AtomicBoolean(false);

	static {
		init();
	}

	/**
	 * 缓存文书正文中出现的图片
	 */
	private static Map<String, byte[]> imageBuffMap = new HashMap<String, byte[]>();

	/**
	 * 添加图片内容缓存
	 *
	 * @param ossName
	 * @param imageBuff
	 */
	public static void putImage(String ossName, byte[] imageBuff) {
		imageBuffMap.put(ossName, imageBuff);
	}

	/**
	 * 清空图片内容缓存
	 */
	public static void clearImage() {
		imageBuffMap.clear();
	}

	/**
	 * 加载字体.<br/>
	 * 此方法推荐各业务系统在项目启动时手动调用，避免第一次生成pdf时出现卡顿现象
	 */
	public static void init() {
		if (!hasInit.compareAndSet(false, true)) {
			return;
		}

		for (PdfFont pdfFont : PdfFont.getFonts()) {

			try {
				byte[] fontBytes = CommonUtil.readFileToBuffer("font/" +  pdfFont.getFontPath());

				String fontName = pdfFont.getFontPath();

				BaseFont bf = BaseFont.createFont(fontName, BaseFont.IDENTITY_H, BaseFont.EMBEDDED, true, fontBytes, null);

				pdfFontMap.put(pdfFont.toString(), bf);

			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}

		// 默认为宋体
		defaultFont = pdfFontMap.get(PdfFont.getFonts().get(0).toString());

//		// 从本次配置路径获取字体
//		String fontPath = DocumentConfig.getConfig("com.haigui.document.font.path");
//
//
//		if (CommonUtil.isNotEmpty(fontPath)) {
//			for (PdfFont pdfFont : PdfFont.getFonts()) {
//
//				try {
//					byte[] fontBytes = readFileFromLocal(fontPath + File.separator + pdfFont.getFontPath());
//
//					String fontName = pdfFont.getFontPath();
//
//					BaseFont bf = BaseFont.createFont(fontName, BaseFont.IDENTITY_H, BaseFont.EMBEDDED, true, fontBytes, null);
//
//					pdfFontMap.put(pdfFont.toString(), bf);
//
//				} catch (Exception e) {
//					log.error(e.getMessage(), e);
//				}
//			}
//		}

		// 从cdn获取字体文件
//		else {
//
//			String fontCachePath = System.getProperty("user.home") + File.separator + "haigui_document_fonts";
//
//			for (PdfFont pdfFont : PdfFont.getFonts()) {
//
//				String cachePath = fontCachePath + File.separator + pdfFont.getFontPath();
//				// 先判断字体文件是否在缓存目录中存在
//				File cacheFile = new File(cachePath);
//				if (cacheFile.exists()) {
//					try {
//						byte[] fontBytes = readFileFromLocal(cachePath);
//
//						// check shar256
//						String shar256 = CommonUtil.sha256(fontBytes);
//
//						// shar 256 对比成功，说明缓存文件可用
//						if (shar256.equals(fontSha256.get(pdfFont.toString()))) {
//							String fontName = pdfFont.getFontPath();
//							BaseFont bf = BaseFont.createFont(fontName, BaseFont.IDENTITY_H, BaseFont.EMBEDDED, true, fontBytes, null);
//							pdfFontMap.put(pdfFont.toString(), bf);
//							log.info("字体文件加载成功, {}, fontName: {}, path: {} ", pdfFont.getName(), fontName, cachePath);
//							continue;
//						} else {
//							cacheFile.delete();
//						}
//					} catch (Exception e) {
//						log.error(e.getMessage(), e);
//					}
//				}
//
//				// 从缓存目录读取失败，改为从 http cdn 路径获取
//				try {
//					String httpPath = "https://haigui-static.oss-cn-hangzhou.aliyuncs.com/fonts";
//
//					byte[] fontBytes = readFileFromUrl(httpPath + File.separator + pdfFont.getFontPath());
//
//					String shar256 = CommonUtil.sha256(fontBytes);
//
//					// shar 256 对比成功，说明缓存文件可用
//					if (shar256.equals(fontSha256.get(pdfFont.toString()))) {
//						String fontName = pdfFont.getFontPath();
//						BaseFont bf = BaseFont.createFont(fontName, BaseFont.IDENTITY_H, BaseFont.EMBEDDED, true, fontBytes, null);
//						pdfFontMap.put(pdfFont.toString(), bf);
//
//						log.info("字体文件加载成功, {}, fontName: {}, path: {} ", pdfFont.getName(), fontName, httpPath + File.separator + pdfFont.getFontPath());
//
//						// 开始缓存字体至本机
//						FileOutputStream fos = null;
//						try {
//							CommonUtil.makeSureFolderExists(cachePath);
//							fos = new FileOutputStream(cachePath);
//							fos.write(fontBytes);
//
//							log.info("字体文件缓存成功, {}, fontName: {}, path: {} ", pdfFont.getName(), fontName, cachePath);
//						} finally {
//							try {
//								fos.close();
//							} catch (Exception e) {
//							}
//						}
//
//						continue;
//					} else {
//						throw new BizException("字体文件下载失败: " + pdfFont.getName() + " url: " + httpPath + File.separator + pdfFont.getFontPath());
//					}
//				} catch (Exception e) {
//					log.error(e.getMessage(), e);
//				}
//			}
//		}

//		// 默认为宋体
//		defaultFont = pdfFontMap.get(PdfFont.getFonts().get(0).toString());

	}

	/**
	 * 获取pdf总页数
	 *
	 * @param data
	 * @return
	 * @author Wang Xiaobo 2019年11月17日
	 */
	public static int getPdfPages(byte[] data) {
		int pages = 0;
		try {
			PdfReader pdfReader = new PdfReader(new ByteArrayInputStream(data));
			pages = pdfReader.getNumberOfPages();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return pages;
	}

	/**
	 * 字符串截取
	 *
	 * @param input 输入源
	 * @param start 开始位置
	 * @param end   结束位置
	 * @return
	 */
	private static String substr(String input, String start, String end) {
		if (input == null) {
			return null;
		}
		int index = input.indexOf(start);
		int index2 = input.indexOf(end, index + start.length());
		if (index == -1 || index2 == -1 || index >= index2) {
			return null;
		}

		return input.substring(index + start.length(), index2);
	}

	public static byte[] readFileFromLocal(String filePath) {
		byte[] result = null;
		InputStream is = null;
		ByteArrayOutputStream bos = null;
		try {
			is = new FileInputStream(filePath);
			bos = new ByteArrayOutputStream();

			byte[] buf = new byte[4096];
			int len = 0;
			while ((len = is.read(buf)) != -1) {
				bos.write(buf, 0, len);
			}

			result = bos.toByteArray();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					log.error("close inputstream error");
				}
			}
			if (bos != null) {
				try {
					bos.close();
				} catch (IOException e) {
					log.error("close outputstream error");
				}
			}
		}
		return result;
	}

	private static byte[] readFileFromUrl(String filePath) {
		byte[] result = null;
		InputStream is = null;
		ByteArrayOutputStream bos = null;
		try {
			URL url = new URL(filePath);
			is = url.openStream();
			bos = new ByteArrayOutputStream();

			byte[] buf = new byte[4096];
			int len = 0;
			while ((len = is.read(buf)) != -1) {
				bos.write(buf, 0, len);
			}

			result = bos.toByteArray();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					log.error("close inputstream error");
				}
			}
			if (bos != null) {
				try {
					bos.close();
				} catch (IOException e) {
					log.error("close outputstream error");
				}
			}
		}
		return result;
	}

	public static BaseFont getDefaultFont() {
		return defaultFont;
	}

	public static BaseFont getBaseFont(PdfFont pdfFont) {
		return pdfFontMap.getOrDefault(pdfFont.toString(), defaultFont);
	}

	public static class MyFontsProvider extends XMLWorkerFontProvider {
		public MyFontsProvider() {
			super(null, null);
		}

		@Override
		public Font getFont(final String fontname, String encoding, float size, final int style) {
			try {

				if (fontname == null) {
					return new Font(defaultFont, size, style);
				}
				if (pdfFontMap.get(fontname) != null) {
					return new Font(pdfFontMap.get(fontname), size, style);
				}

				return new Font(defaultFont, size, style);

			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
			return null;
		}
	}


	static String base64Tag = "base64,";

	public static class ImageTagBase64Processor extends com.itextpdf.tool.xml.html.Image{

		@Override
		public List<Element> end(final WorkerContext ctx, final Tag tag, final List<Element> currentContent){

			List<Element> elements = new ArrayList<Element>(1);

			try{
				final Map<String, String> attributes = tag.getAttributes();
				String src = attributes.get(HTML.Attribute.SRC);

				int index = src.indexOf(base64Tag);

				String base64 = src.substring(index + base64Tag.length() );

				byte[] busf = CommonUtil.base64Decode(base64);

				Image img = Image.getInstance(busf);

				final HtmlPipelineContext htmlPipelineContext = getHtmlPipelineContext(ctx);
				elements.add(getCssAppliers().apply(new Chunk((Image) getCssAppliers().apply(img, tag, htmlPipelineContext), 0, 0, true), tag, htmlPipelineContext));

			}
			catch(Exception e){
			    log.error(e.getMessage(), e);
			}
			return elements;
		}
	}

	/**
	 * 图片base64
	 */
	static class ImageTagProcessor extends com.itextpdf.tool.xml.html.Image {

		@Override
		public List<Element> end(final WorkerContext ctx, final Tag tag, final List<Element> currentContent) {
			final Map<String, String> attributes = tag.getAttributes();
			String src = attributes.get(HTML.Attribute.SRC);
			List<Element> elements = new ArrayList<Element>(1);
			if (null != src && src.length() > 0) {
				Image img = null;
				try {
					int index = src.indexOf("objectName=");
					if (index != -1) {

						String ossName = src.substring(index + "objectName=".length());

						byte[] imageCache = imageBuffMap.get(ossName);

						if (imageCache == null) {
							elements = super.end(ctx, tag, currentContent);
							return elements;
						}

						img = Image.getInstance(imageCache);

						if (img != null) {
							final HtmlPipelineContext htmlPipelineContext = getHtmlPipelineContext(ctx);
							elements.add(getCssAppliers().apply(new Chunk((Image) getCssAppliers().apply(img, tag, htmlPipelineContext), 0, 0, true), tag, htmlPipelineContext));
						}

					}
				} catch (Exception e) {
					e.printStackTrace();
				}

				if (img == null) {
					elements = super.end(ctx, tag, currentContent);
				}
			}
			return elements;
		}
	}

	public static class BackgroundImgHelper extends PdfPageEventHelper {
		private byte[] backgroundImage;

		public BackgroundImgHelper(byte[] backgroundImage) {
			this.backgroundImage = backgroundImage;
		}

		public void onStartPage(PdfWriter writer, Document document) {
			try {
				Image tImgCover = Image.getInstance(backgroundImage);
				/* 设置图片的位置 */
				tImgCover.setAbsolutePosition(0, 0);
				/* 设置图片的大小 */
				tImgCover.scaleAbsolute(595, 842);

				document.add(tImgCover);
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}
	}

	/**
	 * html转pdf（最基础的生成方法，不包含页码、签章、p图等功能）
	 *
	 * @param htmlContent     htmlContent html原文
	 * @param backgroundImage 背景图 尺寸：595* 842
	 * @return
	 */
	public static byte[] generatePdfByHtml(String htmlContent, byte[] backgroundImage) {
		return generatePdfByHtml(htmlContent, backgroundImage, false);
	}


	public static byte[] generatePdfByHtml(String htmlContent, byte[] backgroundImage, boolean isHorizontal){
		return generatePdfByHtml(htmlContent, backgroundImage, isHorizontal, 0, 0, 0);
	}

	/**
	 * html转pdf（最基础的生成方法，不包含页码、签章、p图等功能）
	 *
	 * @param htmlContent     html原文
	 * @param backgroundImage 背景图 尺寸：595* 842
	 * @param isHorizontal    是否横向, 默认 false
	 * @return
	 */
	public static byte[] generatePdfByHtml(String htmlContent, byte[] backgroundImage, boolean isHorizontal, float pageWidth, float pageHeight, int padding ) {
		try {
			long l1 = System.currentTimeMillis();

			ByteArrayInputStream is = new ByteArrayInputStream(htmlContent.getBytes("utf-8"));
			ByteArrayOutputStream bos = new ByteArrayOutputStream();

			Document document = null;

			if(pageWidth>0 && pageHeight>0){
				document = new Document(new RectangleReadOnly(pageWidth, pageHeight), padding, padding, padding, padding);
			}
			else{
				if (isHorizontal) {
					document = new Document(A4_horizontal, left, right, top, bottom);
				} else {
					document = new Document(A4_vertical, left, right, top, bottom);
				}
			}

			PdfWriter writer = PdfWriter.getInstance(document, bos);

			// 背景图不为空
			if (backgroundImage != null) {
				BackgroundImgHelper headerFooter = new BackgroundImgHelper(backgroundImage);
				writer.setPageEvent(headerFooter);
			}
			document.open();

			// 字体处理
			MyFontsProvider fontProvider = new MyFontsProvider();
			fontProvider.addFontSubstitute("", "");
			fontProvider.setUseUnicode(true);

			// 图片base64
			final TagProcessorFactory tagProcessorFactory = Tags.getHtmlTagProcessorFactory();
			tagProcessorFactory.removeProcessor(HTML.Tag.IMG);
			// tagProcessorFactory.addProcessor(new ImageTagProcessor(), HTML.Tag.IMG);
			tagProcessorFactory.addProcessor(new ImageTagBase64Processor(), HTML.Tag.IMG);


			// 使用我们的字体提供器，并将其设置为unicode字体样式
			CssAppliers cssAppliers = new CssAppliersImpl(fontProvider);
			HtmlPipelineContext htmlContext = new HtmlPipelineContext(cssAppliers);
			htmlContext.setTagFactory(tagProcessorFactory);

			CSSResolver cssResolver = new StyleAttrCSSResolver();

			Pipeline<?> pipeline = new CssResolverPipeline(cssResolver, new HtmlPipeline(htmlContext, new PdfWriterPipeline(document, writer)));
			XMLWorker worker = new XMLWorker(pipeline, true);
			XMLParser p = new XMLParser(worker);
			p.parse(new InputStreamReader(is, "UTF-8"));

			document.close();

			long l2 = System.currentTimeMillis();

			byte[] out = bos.toByteArray();

			log.info("[pdf] html to pdf cost: " + (l2 - l1) + "ms");
			return out;

		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * html转pdf， 只做简单转换， 不做其他处理
	 *
	 * @param htmlContent
	 * @return
	 */
	public static byte[] generatePdfByHtml(String htmlContent) {
		byte[] backgroundImage = null;
		return generatePdfByHtml(htmlContent, backgroundImage);
	}

	/**
	 * html转pdf
	 *
	 * @param htmlContent      html文本
	 * @param paginationConfig 分页参数
	 * @return
	 */
	public static byte[] generatePdfByHtml(String htmlContent, PaginationConfig paginationConfig) {
		DocumentResult documentResult = generatePdfByHtml(htmlContent, null, paginationConfig, null, null);
		return documentResult.getPdfBytes();
	}

	/**
	 * html转pdf
	 *
	 * @param htmlContent      html 文本
	 * @param paginationConfig 分页参数
	 * @return
	 */
	public static byte[] generatePdfByHtml(String htmlContent, String paginationConfig) {
		PaginationConfig paginationConfigParam = JsonUtil.fromJson(paginationConfig, PaginationConfig.class);
		DocumentResult documentResult = generatePdfByHtml(htmlContent, null, paginationConfigParam, null, null);
		return documentResult.getPdfBytes();
	}

	/**
	 * html转pdf， 是否使用默认签章进行预览
	 *
	 * @param htmlContent      html 文本
	 * @param paginationConfig 分页参数
	 * @param offsetX          偏移量X
	 * @param offsetY         偏移量Y
	 * @return
	 */
//	public static byte[] generatePdfByHtml(String htmlContent, String paginationConfig, int offsetX, int offsetY) {
//
//		List<FloatImage> imageParams = new ArrayList<>(1);
//
//		FloatImage param = new FloatImage(imgBuff, 140, 140, SignaturePosition, offsetX, offsetY);
//
//		imageParams.add(param);
//
//		PaginationConfig paginationConfigParam = null;
//		if (CommonUtil.isNotEmpty(paginationConfig)) {
//			paginationConfigParam = JsonUtil.fromJson(paginationConfig, PaginationConfig.class);
//		}
//
//		DocumentResult documentResult = PdfGeneratorUtil.generatePdfByHtml(htmlContent, SignaturePosition, paginationConfigParam, imageParams, null);
//
//		return documentResult.getPdfBytes();
//	}

	private static Rectangle A4_vertical = new RectangleReadOnly(595, 842);

	private static Rectangle A4_horizontal = new RectangleReadOnly(842, 595);

	static Executor ex = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

	public static class PdfJob {

		private String html;

		private Boolean isHorizontal;

		public PdfJob(String html, Boolean isHorizontal) {
			this.html = html;
			this.isHorizontal = isHorizontal;
		}

		public String getHtml() {
			return html;
		}

		public void setHtml(String html) {
			this.html = html;
		}

		public Boolean getIsHorizontal() {
			return isHorizontal;
		}

		public void setIsHorizontal(Boolean isHorizontal) {
			this.isHorizontal = isHorizontal;
		}

	}

	/**
	 * 生成pdf内部方法
	 *
	 * @param htmlContent      html内容
	 * @param signatureKeyword 签章关键字
	 * @param autoHeight       是否自动调整行高
	 * @param backgroundImage  背景图（可为空）
	 * @return
	 * @throws Exception
	 */
	private static DocumentResult generatePdf(String htmlContent, String signatureKeyword, Boolean autoHeight, Boolean isHorizontal, byte[] backgroundImage) {
		try {

			String html = htmlContent;

			byte[] pdfByte = null;

			// 签章关键字可能夹杂在正文中， 需要从正文中扣出来，并将关键字浮动显示在正文上
			if (signatureKeyword != null && html.indexOf(signatureKeyword) != -1) {

				// 最终结果
				ByteArrayOutputStream output = new ByteArrayOutputStream();

				// 第1份pdf，用于定位关键字所在位置
				byte[] pdfData = generatePdfByHtml(html, backgroundImage, isHorizontal);

				if (autoHeight != null && autoHeight) {
					DocumentResult documentResult = PdfAutoheightUtil.optimizeLineHeight(pdfData, html);
					pdfData = documentResult.getPdfBytes();
					html = documentResult.getHtmlContent();
				}

				PdfReader pdfReader = new PdfReader(pdfData);

				PdfReaderContentParser parser = new PdfReaderContentParser(pdfReader);

				List<ReplaceRegion> positions = new ArrayList<ReplaceRegion>();

				int pages = pdfReader.getNumberOfPages();

				for (int i = 0; i < pages; i++) {

					int page = i + 1;

					PositionRenderListener listener = new PositionRenderListener(signatureKeyword);

					parser.processContent(page, listener);

					ReplaceRegion replaceRegion = listener.getResult();

					if (replaceRegion != null) {
						replaceRegion.setPage(page);
						positions.add(replaceRegion);
					}

				}
				pdfReader.close();

				// 第2份pdf，取出关键字重新生成一份pdf，并且将关键字做为浮层 定位到原先位置
				if (positions.size() > 0) {
					// 签章所在的段落对方方式是否居右
					boolean alignRight = false;

					int index1 = html.indexOf(signatureKeyword);
					if (index1 != -1) {

						String tmp = html.substring(0, index1);
						tmp = tmp.substring(tmp.lastIndexOf("<p"));

						tmp = substr(tmp, "text-align:", ";");
						if (tmp != null) {
							tmp = tmp.trim();
						}
						if (tmp != null && tmp.equals("right")) {
							alignRight = true;
						}
					}

					html = CommonUtil.replaceAll(html, signatureKeyword, "");

					// 把关键字清除掉，再生成一份pdf
					pdfData = generatePdfByHtml(html, backgroundImage);

					pdfReader = new PdfReader(pdfData);

					PdfStamper stamper = new PdfStamper(pdfReader, output);

					BaseFont font = defaultFont;

					// 签章所在div 居右对齐 的特殊处理
					for (ReplaceRegion position : positions) {
						int page = position.getPage();
						float x = position.getX();
						if (alignRight) {
							x += 12;
						}
						float y = position.getY();

						PdfContentByte canvas = stamper.getOverContent(page);

						canvas.saveState();
						canvas.setColorFill(BaseColor.WHITE);
						canvas.rectangle(x, y, 0, 0);

						canvas.fill();
						canvas.restoreState();
						// 开始写入文本
						canvas.beginText();
						canvas.setFontAndSize(font, 10);

						PdfGState gs = new PdfGState();
						gs.setFillOpacity(0.0f);// 设置透明度为0.0
						canvas.setGState(gs);
						canvas.setTextMatrix(x + 2, y + 2/* 修正背景与文本的相对位置 */);
						canvas.showText(signatureKeyword);

						canvas.endText();
					}
					output.flush();
					stamper.close();

					pdfByte = output.toByteArray();
				}
			} else {

				// 1份pdf
				pdfByte = generatePdfByHtml(html, backgroundImage, isHorizontal);

				if (autoHeight) {
					DocumentResult documentResult = PdfAutoheightUtil.optimizeLineHeight(pdfByte, html);
					pdfByte = documentResult.getPdfBytes();
					html = documentResult.getHtmlContent();
				}

			}

			DocumentResult documentResult = new DocumentResult(pdfByte, html);

			return documentResult;

		} catch (Exception e) {

			throw new RuntimeException(e);
		}
	}

	/**
	 * 换页符
	 */
	private final static String splitRegex = "(<p>)?<p class=\"(SPLIT_LINE|SPLIT_LINE_H)\">.*?</p>(<br/>)?(</p>)?";

	public static void main(String[] args) throws Exception {

		byte[] bufs = readFileFromLocal("/Users/xiaobowang/Downloads/aaaaaa.html");

		String htmlContent = new String(bufs, "utf-8");

		DocumentResult result = generatePdfByHtml(htmlContent, null, null, null, null);

		new FileOutputStream("/Users/xiaobowang/Downloads/AA.pdf").write(result.getPdfBytes());

//		List<String> splitLines = CommonUtil.getMatchedStrs(htmlContent, splitRegex);
//
//		for (String string : splitLines) {
//			System.out.println(string);
//		}
//
//		List<PdfJob> pdfJobList = new ArrayList<PdfJob>();
//
//		String tmpHtmlContent = new String(htmlContent);
//		for (int i = -1; i < splitLines.size(); i++) {
//
//			// 是否横向
//			boolean isHor = false;
//			String html = null;
//
//			// 最后一段
//			if (i == splitLines.size() - 1) {
//				isHor = splitLines.get(i).indexOf("SPLIT_LINE_H") != -1;
//
//				html = tmpHtmlContent;
//			}
//			// 第一段 中间
//			else {
//
//				String tmpSplitLine = splitLines.get(i + 1);
//
//				if (i != -1) {
//					isHor = splitLines.get(i).indexOf("SPLIT_LINE_H") != -1;
//				}
//
//				int tmpIndex = tmpHtmlContent.indexOf(tmpSplitLine);
//
//				html = tmpHtmlContent.substring(0, tmpIndex);
//
//				tmpHtmlContent = tmpHtmlContent.substring(html.length() + tmpSplitLine.length());
//			}
//
//			pdfJobList.add(new PdfJob(html.trim(), isHor));
//
//		}
//		for (PdfJob pdfJob : pdfJobList) {
//			System.out.println(pdfJob.isHorizontal + "\t" + pdfJob.html);
//		}

	}

	public static List<PdfJob> getSplitPdfJobs(String htmlContent) {
		// 有分页符 需要分页处理

		List<PdfJob> pdfJobList = new ArrayList<PdfJob>();

		List<String> splitLines = CommonUtil.getMatchedStrs(htmlContent, splitRegex);
		if (splitLines == null || splitLines.size() == 0) {
			pdfJobList.add(new PdfJob(htmlContent, false));
			return pdfJobList;
		}

		String htmlHead = htmlContent.substring(0, htmlContent.indexOf("<body>") + 6);
		String htmlEnd = "</body></html>";
		String htmlBody = substr(htmlContent, "<body>", "</body>");

		// --------- 拆解pdf任务 ---------
		String tmpHtmlContent = htmlBody;
		for (int i = -1; i < splitLines.size(); i++) {
			// 是否横向
			boolean isHor = false;
			String html = null;

			// 最后一段
			if (i == splitLines.size() - 1) {
				isHor = splitLines.get(i).indexOf("SPLIT_LINE_H") != -1;

				html = tmpHtmlContent;
			}
			// 第一段、中间
			else {

				String tmpSplitLine = splitLines.get(i + 1);

				if (i != -1) {
					isHor = splitLines.get(i).indexOf("SPLIT_LINE_H") != -1;
				}

				int tmpIndex = tmpHtmlContent.indexOf(tmpSplitLine);

				html = tmpHtmlContent.substring(0, tmpIndex);

				tmpHtmlContent = tmpHtmlContent.substring(html.length() + tmpSplitLine.length());
			}
			if (html != null && !html.matches("\\s*")) {
				pdfJobList.add(new PdfJob(htmlHead + html.trim() + htmlEnd, isHor));
			}
		}
		return pdfJobList;
	}

	/**
	 * html转pdf. 最完整的调用方式，支持图片P图，根据关键字P图，分页，添加背景图
	 *
	 * @param htmlContent           html内容
	 * @param signatureKeywordInput 签章关键字（用于签章预处理）
	 * @param paginationConfig      页码参数
	 * @param imageParams           P图参数
	 * @param backgroundImage       背景图参数
	 * @return
	 */
	public static DocumentResult generatePdfByHtml(String htmlContent, String signatureKeywordInput, PaginationConfig paginationConfig, List<FloatImage> imageParams, byte[] backgroundImage) {

		Callable<DocumentResult> callable = new Callable<DocumentResult>() {

			public DocumentResult call() {

				long l1 = System.currentTimeMillis();

				DocumentResult finalResult = null;

				String signatureKeyword = signatureKeywordInput;
				if (signatureKeyword == null) {
					signatureKeyword = SignaturePosition;
				}

				// 判断是否有分页符， 如需要， 则分页处理
				List<PdfJob> pdfJobList = getSplitPdfJobs(htmlContent);
				if (pdfJobList != null && pdfJobList.size() > 0) {

					// --------- 开始生成 pdf ------------
					List<byte[]> pdfByteList = new ArrayList<byte[]>(pdfJobList.size());
					for (int kk = 0; kk < pdfJobList.size(); kk++) {

						boolean autoHeight = false;

						if (kk == 0 && paginationConfig != null && "y".equals(paginationConfig.getAutoLineHeight())) {
							autoHeight = true;
						}

						PdfJob pdfJob = pdfJobList.get(kk);

						DocumentResult tmpPdfResult = generatePdf(pdfJob.html, signatureKeyword, autoHeight, pdfJob.isHorizontal, backgroundImage);
						pdfByteList.add(tmpPdfResult.getPdfBytes());

					}

					// --------- 合并 pdf ---------
					byte[] pdfResult = null;
					try {
						Document document = new Document(PageSize.A4, left, right, top, bottom);
						ByteArrayOutputStream bos = new ByteArrayOutputStream();
						PdfCopy copy = new PdfCopy(document, bos);
						document.open();
						for (byte[] bytes : pdfByteList) {
							PdfReader reader = new PdfReader(bytes);
							int n = reader.getNumberOfPages(); // 获得总页码
							for (int j = 1; j <= n; j++) {
								document.newPage();
								PdfImportedPage page = copy.getImportedPage(reader, j);
								copy.addPage(page);
							}
						}
						document.close();
						bos.close();
						pdfResult = bos.toByteArray();

					} catch (Exception e) {
						log.error(e.getMessage(), e);
						throw new RuntimeException(e);
					}

					finalResult = new DocumentResult(pdfResult, htmlContent);

				}
				// 无分页符
				else {

					boolean autoHeight = false;

					if (paginationConfig != null && "y".equals(paginationConfig.getAutoLineHeight())) {
						autoHeight = true;
					}

					finalResult = generatePdf(htmlContent, signatureKeyword, autoHeight, false, backgroundImage);
				}

				log.info("[html to pdf] cost " + (System.currentTimeMillis() - l1) + "ms");

				return finalResult;
			}
		};

		FutureTask<DocumentResult> task = new FutureTask<DocumentResult>(callable);

		Thread t1 = new Thread(task, "pdf");
		t1.start();

		DocumentResult documentResult = null;

//		try {
//			documentResult = task.get();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}

		try {
			documentResult = task.get(10000, TimeUnit.MILLISECONDS);

			if (documentResult == null) {
				throw new BizException("pdf生成失败， 原因：超时");
			}

		} catch (TimeoutException e) {

			log.error(e.getMessage(), e);

			try {
				t1.stop();
				t1 = null;
			} catch (Exception e2) {
			}
			throw new BizException("pdf生成超时");

		} catch (Exception e) {

			log.error(e.getMessage(), e);

			try {
				t1.stop();
				t1 = null;
			} catch (Exception e2) {
			}
			throw new BizException("html转pdf失败：" + e.getMessage());

		}

		task = null;
		t1 = null;

		return documentResult;
	}

	/**
	 * 抽取pdf中不含空白字符（包括换行符等）的文本
	 *
	 * @param pdfData
	 */
//	public static String extractTextWithoutWhitespace(byte[] pdfData) {
//		try (InputStream inputStream = new ByteArrayInputStream(pdfData)) {
//			PdfReader reader = new PdfReader(inputStream);
//			int numberOfPages = reader.getNumberOfPages();
//			StringBuilder sb = new StringBuilder();
//			for (int i = 1; i <= numberOfPages; i++) {
//				String text = PdfTextExtractor.getTextFromPage(reader, i);
//				sb.append(text);
//			}
//			return StringUtils.trimAllWhitespace(sb.toString());
//		} catch (Exception e) {
//			log.error("[PdfGeneratorUtil] extractTextWithoutWhitespace error ", e);
//			throw new BizException("提取pdf文本失败");
//		}
//	}

}
