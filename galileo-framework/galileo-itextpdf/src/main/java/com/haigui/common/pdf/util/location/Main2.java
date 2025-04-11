package com.haigui.common.pdf.util.location;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfGState;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.parser.PdfReaderContentParser;

public class Main2 {

	public static void main(String[] args) throws IOException, DocumentException {

		File pdfFile = new File("/Users/xiaobowang/Desktop/out/text.pdf");

		FileOutputStream output = new FileOutputStream("/Users/xiaobowang/Desktop/out/text_new.pdf");

		String keyword = "李勇信用卡纠";// "[[signature_here]]"; // DocumentConstant.SignaturePosition;

		byte[] pdfData = new byte[(int) pdfFile.length()];
		FileInputStream inputStream = null;
		try {
			inputStream = new FileInputStream(pdfFile);
			inputStream.read(pdfData);
		} catch (IOException e) {
			throw e;
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
				}
			}
		}

		// List<float[]> positions = PdfKeywordFinder.findKeywordPostions(pdfData, keyword);

		// ******************
		PdfReader pdfReader = new PdfReader(pdfData);

		PdfStamper stamper = new PdfStamper(pdfReader, output);

		// BaseFont font = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.EMBEDDED);

		PdfReaderContentParser parser = new PdfReaderContentParser(pdfReader);

		List<ReplaceRegion> positions = new ArrayList<ReplaceRegion>();

		int pages = pdfReader.getNumberOfPages();
		for (int i = 0; i < pages; i++) {
			int page = i + 1;

			PositionRenderListener listener = new PositionRenderListener(keyword);

			parser.processContent(page, listener);

			ReplaceRegion replaceRegion = listener.getResult();

			if (replaceRegion != null) {
				replaceRegion.setPage(page);
				positions.add(replaceRegion);
			}
		}

		// ******************

		BaseFont font = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.EMBEDDED);

		for (ReplaceRegion position : positions) {
			int page = position.getPage();
			float x = position.getX();
			float y = position.getY();

			PdfContentByte canvas = stamper.getOverContent(page);

			canvas.saveState();
			canvas.setColorFill(BaseColor.RED);
			canvas.rectangle(x, y, 0, 0);

			canvas.fill();
			canvas.restoreState();
			// 开始写入文本
			canvas.beginText();
			canvas.setFontAndSize(font, 10);

			PdfGState gs = new PdfGState();
			gs.setFillOpacity(0.3f);// 设置透明度为0.8
			canvas.setGState(gs);
			canvas.setTextMatrix(x + 2, y + 2/* 修正背景与文本的相对位置 */);
			canvas.showText("####");

			canvas.endText();


		}
		output.flush();
		stamper.close();

		output.flush();
		stamper.close();

	}
}
