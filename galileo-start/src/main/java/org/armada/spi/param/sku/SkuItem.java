package org.armada.spi.param.sku;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author xiaobo
 * @date 2021/4/26 11:53 上午
 */
@Data
@Accessors(chain = true)
public class SkuItem {

    /**
     * 商品条码
     */
    private String skuBarCode;

//    /**
//     * 货主
//     */
//    private String ownerCode;

    /**
     * 货主
     */
    private String customerCode;

    /**
     * 数量
     */
    private Integer qty;

    /**
     * 商品等级	0：正  1：残
     */
    private Integer qualityLevel;

}
