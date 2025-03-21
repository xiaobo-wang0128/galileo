package com.haigui.common.pdf.util;//package com.haigui.common.pdf.util;
//
//import java.io.ByteArrayOutputStream;
//import java.util.ArrayList;
//import java.util.List;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import com.haigui.common.pdf.util.location.PositionRenderListenerAll;
//import com.haigui.common.pdf.util.location.ReplaceRegion;
//import com.haigui.framework.askja.common.JsonUtil;
//import com.haigui.framework.askja.exception.BizException;
//import com.itextpdf.text.pdf.PdfReader;
//import com.itextpdf.text.pdf.PdfStamper;
//import com.itextpdf.text.pdf.parser.PdfReaderContentParser;
//
//public class PdfKeywordFinder {
//
//	private static Logger log = LoggerFactory.getLogger(PdfImageUtil.class);
//
//
//
//	public static List<ReplaceRegion> findKeywordPosition(byte[] pdfBytes, String keyword) {
//		try {
//
//			if (pdfBytes == null || pdfBytes.length == 0) {
//				throw new BizException("pdf数据不能为空");
//			}
//
//			if (keyword == null || keyword.matches("\\s*")) {
//				throw new BizException("关键字不能为空");
//			}
//
//			PdfReader pdfReader = new PdfReader(pdfBytes);
//
//			PdfReaderContentParser parser = new PdfReaderContentParser(pdfReader);
//
//			ByteArrayOutputStream output = new ByteArrayOutputStream();
//
//			PdfStamper stamper = new PdfStamper(pdfReader, output);
//
//			int pages = pdfReader.getNumberOfPages();
//
//			List<ReplaceRegion> replaceRegions = new ArrayList<ReplaceRegion>();
//
//			for (int i = 0; i < pages; i++) {
//				int page = i + 1;
//
//				// 扫描关键字位置
//
//				PositionRenderListenerAll listener = new PositionRenderListenerAll(keyword);
//
//				parser.processContent(page, listener);
//
//				List<ReplaceRegion> tmp = listener.getResultList();
//				if (tmp != null && tmp.size() > 0) {
//
//					replaceRegions.addAll(tmp);
//				}
//			}
//
//			System.out.println(JsonUtil.toJson(replaceRegions));
//
//			output.flush();
//			stamper.close();
//
//			return replaceRegions;
//
//		} catch (Exception e) {
//
//			log.info(e.getMessage(), e);
//
//			throw new BizException(e);
//		}
//	}
//
//}
