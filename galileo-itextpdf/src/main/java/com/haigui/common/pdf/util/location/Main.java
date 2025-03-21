package com.haigui.common.pdf.util.location;//package com.haigui.common.pdf.util.location;
//
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.util.List;
//
//import com.itextpdf.text.BaseColor;
//import com.itextpdf.text.DocumentException;
//import com.itextpdf.text.pdf.BaseFont;
//import com.itextpdf.text.pdf.PdfContentByte;
//import com.itextpdf.text.pdf.PdfReader;
//import com.itextpdf.text.pdf.PdfStamper;
//
//public class Main {
//
//	public static void main(String[] args) throws IOException, DocumentException {
//
//		File pdfFile = new File("/Users/xiaobowang/Desktop/out/text.pdf");
//
//		FileOutputStream output = new FileOutputStream("/Users/xiaobowang/Desktop/out/text_new.pdf");
//
//		String keyword = "[[signature_here]]"; // DocumentConstant.SignaturePosition;
//
//		byte[] pdfData = new byte[(int) pdfFile.length()];
//		FileInputStream inputStream = null;
//		try {
//			inputStream = new FileInputStream(pdfFile);
//			inputStream.read(pdfData);
//		} catch (IOException e) {
//			throw e;
//		} finally {
//			if (inputStream != null) {
//				try {
//					inputStream.close();
//				} catch (IOException e) {
//				}
//			}
//		}
//
//		List<float[]> positions = PdfKeywordFinder.findKeywordPostions(pdfData, keyword);
//
//		// ******************
//		PdfReader pdfReader = new PdfReader(pdfData);
//
//		PdfStamper stamper = new PdfStamper(pdfReader, output);
//
//		BaseFont font = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.EMBEDDED);
//
//		for (float[] position : positions) {
//			int page = (int) position[0];
//			float x = position[1];
//			float y = position[2];
//
//			PdfContentByte canvas = stamper.getOverContent(page);
//
//			canvas.saveState();
//			canvas.setColorFill(BaseColor.RED);
//			canvas.rectangle(x, y, 0, 0);
//
//			canvas.fill();
//			canvas.restoreState();
//			// 开始写入文本
//			canvas.beginText();
//			canvas.setFontAndSize(font, 10);
//			canvas.setTextMatrix(x + 2, y + 2/* 修正背景与文本的相对位置 */);
//			canvas.showText("[[signsign]]");
//
//			canvas.endText();
//
//			System.out.print("pageNum: " + (int) position[0]);
//			System.out.print("\tx: " + position[1]);
//			System.out.println("\ty: " + position[2]);
//		}
//		output.flush();
//		stamper.close();
//
//	}
//}
