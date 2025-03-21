package org.armada.galileo.sample.biz_config.sample_config.sample_constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author ake
 * @date 2021/11/25 16:25
 */
@Getter
@AllArgsConstructor
public enum StocktakeFeedbackTypeEnum {

    /**
     * 盘点回传类型
     */
    STOCK("明细库存维度"),
    SKU("SKU维度");

    private final String descCN;

    private final String label = "库存回传类型";

}
