package com.haigui.common.pdf.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.haigui.common.pdf.constant.Pagination;
import com.haigui.common.pdf.constant.PaginationAlignMode;
import com.haigui.common.pdf.constant.PaginationBuilder;
import com.haigui.common.pdf.constant.PaginationConfig;
import com.haigui.common.pdf.constant.PaginationMode;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;

public class PdfPageUtil {

	private static Logger log = LoggerFactory.getLogger(PdfPageUtil.class);

	/**
	 * 添加页码
	 *
	 * @param bytes
	 * @return
	 */
	public static byte[] addPagination(byte[] bytes, PaginationConfig paginationConfig) {
		long l1 = System.currentTimeMillis();
		paginationConfig = Optional.ofNullable(paginationConfig).orElse(PaginationConfig.defaultConfig()) ; //.orElseGet(PaginationConfig::defaultConfig);
		PdfReader reader = null;
		PdfStamper stamper = null;
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		try {
			// 根据一个pdfreader创建一个pdfStamper.用来生成新的pdf.
			reader = new PdfReader(bytes);
			stamper = new PdfStamper(reader, outputStream);
			// 获得宽
			Rectangle rectangle = reader.getPageSize(1);
			float width = rectangle.getWidth();
			// 获取页码
			int num = reader.getNumberOfPages();
			PaginationBuilder builder = PaginationBuilder.create(num).withShowMode(paginationConfig.getShowMode()).withAlignMode(paginationConfig.getAlignMode());
			for (int i = 1; i <= num; i++) {
				Pagination pagination = builder.build(i);
				PdfContentByte over = stamper.getOverContent(i);
				processStamper(width, over, pagination);
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		} finally {
			if (stamper != null) {
				try {
					stamper.close();
				} catch (DocumentException | IOException e) {
					log.error(e.getMessage(), e);
				}
			}
			if (reader != null) {
				reader.close();
			}
		}

		long l2 = System.currentTimeMillis();

		byte[] out = outputStream.toByteArray();

		log.info("[pdf] add page cost: " + (l2 - l1) + "ms");
		return out;
	}

	// 页码距离底部
	private final static float PAGINATION_BOTTOM = 29.5833F;
	// 页码距离旁边
	private final static float PAGINATION_SIDE = 76.5F;

	/**
	 * 处理不同模式页码
	 *
	 * @param over
	 * @param pagination
	 */
	private static void processStamper(float width, PdfContentByte over, Pagination pagination) {
		String text = pagination.getText();
		int currentPage = pagination.getCurrentPage();
		int totalPage = pagination.getTotalPage();
		float fontSize = pagination.getFontSize();
		float textWidth = pagination.getWidth();
		float ascent = pagination.getAscent();

		float initx, inity = PAGINATION_BOTTOM + ascent;

		PaginationAlignMode alignMode = PaginationAlignMode.parse(pagination.getAlignMode());

		initx = processInitx(width, alignMode, currentPage, textWidth);

		PaginationMode showMode = pagination.getShowMode();
		if (showMode == PaginationMode.NUM_ZH_TOTAL) {
			Pattern compile = Pattern.compile("^(.*)" + currentPage + "(.*)" + totalPage + "(.*)$");
			Matcher matcher = compile.matcher(text);
			if (matcher.find()) {
				float diffX = 0F;
				diffX += processZh(over, matcher.group(1), fontSize, initx, inity);
				diffX += processAscii(over, "" + currentPage, fontSize, initx + diffX, inity);
				diffX += processZh(over, matcher.group(2), fontSize, initx + diffX, inity);
				diffX += processAscii(over, "" + totalPage, fontSize, initx + diffX, inity);
				processZh(over, matcher.group(3), fontSize, initx + diffX, inity);
			}
		}
		else if (showMode == PaginationMode.NUM_ZH) {
			Pattern compile = Pattern.compile("^(.*)" + currentPage + "(.*)$");
			Matcher matcher = compile.matcher(text);
			if (matcher.find()) {
				float diffX = 0F;
				diffX += processZh(over, matcher.group(1), fontSize, initx, inity);
				diffX += processAscii(over, "" + currentPage, fontSize, initx + diffX, inity);
				processZh(over, matcher.group(2), fontSize, initx + diffX, inity);
			}
		}
		else {
			processAscii(over, text, fontSize, initx, inity);
		}
		over.stroke();
	}

	private static float processInitx(float width, PaginationAlignMode alignMode, int currentPage, float textWidth) {
		float initx;
		switch (alignMode) {
		case left:
			initx = PAGINATION_SIDE;
			break;
		case right:
			initx = width - PAGINATION_SIDE - textWidth;
			break;
		case side:
			if (currentPage % 2 == 1) {
				initx = width - PAGINATION_SIDE - textWidth;
			}
			else {
				initx = PAGINATION_SIDE;
			}
			break;
		default:
			initx = (width - textWidth) / 2;
		}
		return initx;
	}

	private static float processAscii(PdfContentByte over, String text, float fontSize, float x, float y) {
		over.beginText();
		over.setTextMatrix(x, y);
		over.setFontAndSize(Pagination.ASCII_FONT, fontSize);
		over.setColorFill(BaseColor.BLACK);
		over.showText(text);
		over.endText();
		return Pagination.ASCII_FONT.getWidthPoint(text, fontSize);
	}

	private static float processZh(PdfContentByte over, String text, float fontSize, float x, float y) {
		over.beginText();
		over.setTextMatrix(x, y);
		over.setFontAndSize(Pagination.ZH_FONT, fontSize);
		over.setColorFill(BaseColor.BLACK);
		over.showText(text);
		over.endText();
		return Pagination.ZH_FONT.getWidthPoint(text, fontSize);
	}

}
