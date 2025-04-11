package com.haigui.common.pdf.util.image;

import java.io.FileOutputStream;
import java.io.IOException;

import com.itextpdf.text.pdf.PRStream;
import com.itextpdf.text.pdf.PdfDictionary;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.parser.ImageRenderInfo;
import com.itextpdf.text.pdf.parser.PdfImageObject;
import com.itextpdf.text.pdf.parser.RenderListener;
import com.itextpdf.text.pdf.parser.TextRenderInfo;

class ImageRenderListener implements RenderListener {
	final String name;
	int counter = 100000;

	byte[] images;

	public ImageRenderListener(String name) {
		this.name = name;
	}

	public void beginTextBlock() {
	}

	public void renderText(TextRenderInfo renderInfo) {
	}

	public void endTextBlock() {
	}

	public void renderImage(ImageRenderInfo renderInfo) {
		try {
			PdfImageObject image = renderInfo.getImage();
			if (image == null)
				return;
			int number = renderInfo.getRef() != null ? renderInfo.getRef().getNumber() : counter++;
			String filename = String.format("%s-%s.%s", name, number, image.getFileType());
			FileOutputStream os = new FileOutputStream(filename);
			os.write(image.getImageAsBytes());
			os.flush();
			os.close();

			PdfDictionary imageDictionary = image.getDictionary();

			for(PdfName pname: imageDictionary.getKeys()) {
				System.out.println(pname.toString());

			}
//			System.out.println(JsonUtil.toJson(imageDictionary.getKeys()));

			PRStream maskStream = (PRStream) imageDictionary.getAsStream(PdfName.IMAGE);
			if (maskStream != null) {
				PdfImageObject maskImage = new PdfImageObject(maskStream);
				filename = String.format("%s-%s-mask.%s", name, number, maskImage.getFileType());
				os = new FileOutputStream("/Users/xiaobowang/Downloads/实名认证信息_img.png");
				os.write(maskImage.getImageAsBytes());
				os.flush();
				os.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
