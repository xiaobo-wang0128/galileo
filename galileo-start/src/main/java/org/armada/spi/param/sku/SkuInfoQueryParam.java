package org.armada.spi.param.sku;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author xiaobo
 * @date 2021/4/22 3:31 下午
 */
@Data
@Accessors(chain = true)
public class SkuInfoQueryParam {

    /**
     * 仓库编码
     */
    private String warehouseCode;

    /**
     * 商品条码
     */
    private String skuBarCode;

}
