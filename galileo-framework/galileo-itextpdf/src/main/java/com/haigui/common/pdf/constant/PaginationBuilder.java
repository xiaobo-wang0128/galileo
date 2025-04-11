package com.haigui.common.pdf.constant;

import org.armada.galileo.common.util.AssertUtil;
import org.armada.galileo.exception.BizException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * @Author: xuhui
 * @Description:
 * @Date: 2020/2/20 下午7:47
 */
public class PaginationBuilder {

    private PaginationBuilder(int totalPage) {
        this.totalPage = totalPage;
    }

    /**
     * 总页码
     */
    private int totalPage = 0;

    private int currentPage = 0;

    /**
     * 单位pt
     */
    private int fontSize = 9;

    /**
     * 排列模式
     */
    private String alignMode = "center";

    /**
     * 暂时模式
     */
    private PaginationMode showMode = PaginationMode.NUM_SIMPLE;

    public static PaginationBuilder create(int totalPage) {
        return new PaginationBuilder(totalPage);
    }

    public Pagination build(int currentPage) {
        if (currentPage > totalPage) {
            throw new BizException("pagination build error，currentPage is " + currentPage + ", totalPage is " + totalPage);
        }

        return new Pagination(showMode, fontSize, currentPage, totalPage,this.alignMode);
    }

    public PaginationBuilder withTotalPage(int totalPage) {
        this.totalPage = totalPage;
        return this;
    }

    public PaginationBuilder withShowMode(int showMode) {
        this.showMode = PaginationMode.parse(showMode);
        return this;
    }

    public PaginationBuilder withShowMode(PaginationMode showMode) {
        AssertUtil.isNotNull(showMode);
        this.showMode = showMode;
        return this;
    }

    public PaginationBuilder withFontSize(int fontSize) {
        this.fontSize = fontSize;
        return this;
    }

    public PaginationBuilder withAlignMode(String alignMode) {
        AssertUtil.isNotNull(alignMode);
        this.alignMode = alignMode;
        return this;
    }

    public static void main(String[] args) {
        String s = "第几12页";
        int currentPage = 12, totalPage = 1234;
        Pattern compile = Pattern.compile("(.*)" + currentPage + "(.*)" + totalPage + "(.*)");
        Matcher matcher = compile.matcher(s);
        if (matcher.find()) {
            System.out.println(matcher.group(1));
            System.out.println(matcher.group(2));
            System.out.println(matcher.group(3));
        }

    }

}
