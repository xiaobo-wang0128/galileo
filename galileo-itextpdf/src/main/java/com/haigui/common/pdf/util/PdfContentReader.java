package com.haigui.common.pdf.util;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.itextpdf.awt.geom.Rectangle2D;
import com.itextpdf.awt.geom.RectangularShape;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.parser.ImageRenderInfo;
import com.itextpdf.text.pdf.parser.PdfReaderContentParser;
import com.itextpdf.text.pdf.parser.RenderListener;
import com.itextpdf.text.pdf.parser.TextRenderInfo;

public class PdfContentReader {

//	public static void main(String[] args) {
//		try {
//			PdfReader reader = new PdfReader("/Users/xiaobowang/Downloads/qisuzhauang.pdf");
//			// 新建一个PDF解析对象
//			PdfReaderContentParser parser = new PdfReaderContentParser(reader);
//			// 包含了PDF页面的信息，作为处理的对象
//			PdfStamper stamper = new PdfStamper(reader, new FileOutputStream("/Users/xiaobowang/Downloads/1357_合同222.pdf"));
//			// 获取pdf的页数
//			PdfContentByte page = stamper.getOverContent(1);
//			for (int i = 1; i <= reader.getNumberOfPages(); i++) {
//				// 新建一个ImageRenderListener对象，该对象实现了RenderListener接口，作为处理PDF的主要类
//				TestRenderListener listener = new TestRenderListener();
//				// 解析PDF，并处理里面的文字
//				parser.processContent(i, listener);
//				// 获取文字的矩形边框
//				List<Rectangle2D.Float> rectText = listener.rectText;
//				List<String> textList = listener.textList;
//				List<Float> listY = listener.listY;
//				List<Map<String, Rectangle2D.Float>> list_text = listener.rows_text_rect;
//				Map<String, Position> map1 = new HashMap<>();
//				for (int k = 0; k < list_text.size(); k++) {
//					Map<String, Rectangle2D.Float> map = list_text.get(k);
//					for (Map.Entry<String, Rectangle2D.Float> entry : map.entrySet()) {
//						System.out.println(entry.getKey() + "----" + entry.getValue());
//					}
//				}
//			}
//		} catch (Exception ex) {
//			ex.printStackTrace();
//		}
//	}

	public static class TestRenderListener implements RenderListener {

		// 用来存放文字的矩形
		List<Rectangle2D.Float> rectText = new ArrayList<Rectangle2D.Float>();
		// 用来存放文字
		List<String> textList = new ArrayList<String>();
		// 用来存放文字的y坐标
		List<Float> listY = new ArrayList<Float>();
		// 用来存放每一行文字的坐标位置
		List<Map<String, Rectangle2D.Float>> rows_text_rect = new ArrayList<>();
		// PDF文件的路径
		protected String filepath = null;

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

				System.out.println(text);

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
	}
}
