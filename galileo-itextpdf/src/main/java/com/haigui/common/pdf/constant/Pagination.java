package com.haigui.common.pdf.constant;

import com.haigui.common.pdf.util.PdfGeneratorUtil;
import com.haigui.common.pdf.util.help.PdfFont;
import com.itextpdf.text.pdf.BaseFont;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @Author: xuhui
 * @Description:
 * @Date: 2020/2/21 下午4:28
 */
@Data
@Accessors(chain = true)
public class Pagination {

    /**
     * 中文字体
     */
    public static final BaseFont ZH_FONT = PdfGeneratorUtil.getBaseFont(PdfFont.SimHei);
    /**
     * 非中文字体
     */
    public static final BaseFont ASCII_FONT = PdfGeneratorUtil.getBaseFont(PdfFont.SimHei);


    private float width;

    private float ascent;

    private String text;

    private float fontSize;

    private PaginationMode showMode;

    private String alignMode;

    private int currentPage;

    private int totalPage;

    public Pagination(PaginationMode showMode, float fontSize,int currentPage,int totalPage,String alignMode) {
        this.fontSize = fontSize;
        this.showMode = showMode;
        this.alignMode = alignMode;
        this.text = this.showMode.parseTemplate(currentPage, totalPage);
        this.currentPage = currentPage;
        this.totalPage = totalPage;

        float width, ascent;
        if (this.showMode == PaginationMode.NUM_ZH_TOTAL || this.showMode == PaginationMode.NUM_ZH) {
            float numWidth = ASCII_FONT.getWidthPoint(currentPage+""+totalPage, fontSize);
            String zhText = text.replaceAll("\\d*", "");
            float zhWidth = ZH_FONT.getWidthPoint(zhText,fontSize);
            width = numWidth + zhWidth;
            ascent = Math.max(ASCII_FONT.getAscentPoint(String.valueOf(currentPage), fontSize), this.ZH_FONT.getAscentPoint(zhText, fontSize));
        }else {
            width = ASCII_FONT.getWidthPoint(text, fontSize);
            ascent = ASCII_FONT.getAscentPoint(text, fontSize);
        }
        this.width = width;
        this.ascent = ascent;
    }

}
