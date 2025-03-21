package org.armada.galileo.sample.biz_config.sample_config.sample_constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Jazz
 * @date 2021/3/22 13:55
 */
@Getter
@AllArgsConstructor
public enum StocktakeCreateTypeEnum {
    /**
     * 盘点创建类型
     */
    RACK("货架"),
    TOTE("料箱"),
    SKU("商品"),
    STOCK("库存"),
    CHANGE("动碰(库存变动)");

    private final String descCN;

    private final String label = "盘点创建类型";
}
