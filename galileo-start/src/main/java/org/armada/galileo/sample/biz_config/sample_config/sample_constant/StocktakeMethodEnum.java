package org.armada.galileo.sample.biz_config.sample_config.sample_constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Jazz
 * @date 2021/3/24 16:01
 */
@Getter
@AllArgsConstructor
public enum StocktakeMethodEnum {
    /**
     * 盘点方式
     */
    INFORMED("明盘"),
    BLIND_QTY("盲盘(数量)"),
    BLIND_SKU_QTY("盲盘(商品 + 数量)");

    private final String descCN;

    private final String label = "盘点方式";
}
