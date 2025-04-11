package com.haigui.common.pdf.util.image;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfReaderContentParser;

/**
 * 从pdf读取图片
 * 
 * @author xiaobowang 2020年9月17日
 */
public class PdfImageReaderUtil {

	public static void readImages(byte[] pdfBytes) throws Exception {

		PdfReader pdfReader = new PdfReader(pdfBytes);

		PdfReaderContentParser parser = new PdfReaderContentParser(pdfReader);
		ImageRenderListener listener = new ImageRenderListener("testpdf");

		for (int i = 1; i <= pdfReader.getNumberOfPages(); i++) {
			parser.processContent(i, listener);
		}
	}
}
