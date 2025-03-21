package org.armada.galileo.sample.biz_config.sample_config.sample_constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author buhuo.cai
 * @date 2022/2/15 09:25
 */
@Getter
@AllArgsConstructor
public enum StocktakeProfitStrategyEnum {

    /**
     * 盘盈限制策略
     */
    ALL("初盘、复盘都允许盘盈"),
    FIRST("初盘允许盘盈，复盘不允许盘盈"),
    REVIEW("初盘不允许盘盈，复盘允许盘盈"),
    NOT_ALLOW("不允许盘盈");

    private final String descCN;

    private final String label = "盘盈限制策略";

}
