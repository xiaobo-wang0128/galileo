package com.haigui.common.pdf.constant;

/**
 * @Author: xuhui
 * @Description:页码模式
 * @Date: 2020/2/20 下午8:12
 */
public enum PaginationMode {

    /**
     * 1 2 3
     */
    NUM_SIMPLE(0,"%d"),
    /**
     * 1/5 2/5
     */
    NUM_S_TOTAL(1,"%d/%d"),
    /**
     * 1-5 1-6
     */
    NUM_H_TOTAL(2,"%d-%d"),
    /**
     * 第1页，共5页
     */
    NUM_ZH_TOTAL(3,"第%d页，共%d页"),

    /**
     * 第1页
     */
    NUM_ZH(4, "第%d页"),
    ;

    public final int MODE;

    public final String TEMPLATE;

    PaginationMode(int mode,String template){
        this.MODE = mode;
        this.TEMPLATE = template;
    }

    public String parseTemplate(int currentPage, int totalPage) {
        return String.format(TEMPLATE, currentPage, totalPage);
    }

    public static PaginationMode parse(int mode){
        for (PaginationMode value : values()) {
            if (value.MODE == mode) {
                return value;
            }
        }
        throw new IllegalArgumentException("PaginationMode error");
    }



}
