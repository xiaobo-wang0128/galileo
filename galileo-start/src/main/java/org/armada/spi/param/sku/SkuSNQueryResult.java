package org.armada.spi.param.sku;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author xiaobo
 * @date 2021/4/22 3:54 下午
 */
@Data
@Accessors(chain = true)
public class SkuSNQueryResult {

    /**
     * 仓库
     */
    private String warehouseCode;

    /**
     * 货主
     */
    private String customerCode;

    /**
     * 商品条码
     */
    private String skuBarCode;

    /**
     * 单品状态
     */
    private String status;

}
