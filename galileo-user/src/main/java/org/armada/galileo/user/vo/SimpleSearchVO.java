package org.armada.galileo.user.vo;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author xiaobo
 * @date 2022/2/7 11:30 上午
 */
@Data
@Accessors(chain = true)
public class SimpleSearchVO {
    /**
     * 关键字
     */
    private String keyword;

    /**
     * 状态
     */
    private String status;

    /**
     * 分布参数， 从1开始
     */
    private Integer pageIndex = 1;

    /**
     * 每页显示数量, -1 表示不分页
     */
    private Integer pageSize = 20;

    /**
     * 排序字段
     */
    private String orderby;
}
