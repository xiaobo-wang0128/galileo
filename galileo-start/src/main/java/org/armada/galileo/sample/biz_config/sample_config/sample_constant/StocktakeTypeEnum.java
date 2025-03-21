package org.armada.galileo.sample.biz_config.sample_config.sample_constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Jazz
 * @date 2021/3/23 11:35
 */
@Getter
@AllArgsConstructor
public enum StocktakeTypeEnum {

    /**
     * 盘点类型
     */
    ORDINARY("普通盘点"),
    DISCREPANCY_REVIEW("差异复盘");

    private final String descCN;

    private final String label = "盘点类型";

}
