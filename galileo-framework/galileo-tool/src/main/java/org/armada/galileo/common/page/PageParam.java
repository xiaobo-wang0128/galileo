package org.armada.galileo.common.page;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@Accessors(chain = true)
public class PageParam implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 每页数据
     */
    private Integer pageSize;

    /**
     * 目标页码
     */
    private Integer pageIndex;

    /**
     * 排序类型 asc desc
     */
    private String orderByType;

    /**
     * 排序字段
     */
    private String orderByColumn;

    /**
     * 自定义排序sql， 一般是需要根据多个字段排序时使用
     */
    private String orderByClause;

    /**
     * 是否需要 count 查询
     */
    private Boolean needCount = false;

    /**
     * 是否开启分页
     */
    private Boolean openPage = false;

    /**
     * 用户查总数的字段名， 默认 * ，如 select count(*) from xxx
     * <br/>如配置了该值， 则会被替换成 select count({countWords}) from xxx
     * <br/>本配置可用于解决 count 去重的问题，如 count(distinct xx_id)
     */
    private String countField;

    /**
     * offset 查询方式
     */
    private Integer start;

    /**
     * offset 查询方式
     */
    private Integer limit;


    private PageParam() {
    }

    public static PageParam instanceByPageIndex(Integer pageIndex, Integer pageSize) {
        PageParam pp = new PageParam();
        pp.pageSize = pageSize;
        pp.pageIndex = pageIndex;
        pp.needCount = true;
        return pp;
    }

    public static PageParam instanceByPageIndex(Integer pageIndex, Integer pageSize, Boolean openPage) {
        PageParam pp = new PageParam();
        pp.pageSize = pageSize;
        pp.pageIndex = pageIndex;
        pp.openPage = openPage;
        pp.needCount = true;
        return pp;
    }

    public static PageParam instanceByOffset(Integer start, Integer limit) {
        PageParam pp = new PageParam();
        pp.start = start;
        pp.limit = limit;
        pp.needCount = false;
        return pp;
    }

}
