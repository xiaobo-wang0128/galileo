package org.armada.spi.param.sku;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.TreeMap;

/**
 * @author xiaobo
 * @date 2021/4/22 3:31 下午
 */
@Data
@Accessors(chain = true)
public class SkuSNQueryParam {

    /**
     * map
     */
    private TreeMap<String,Object> map;
    /**
     * 仓库编码
     */
    private String warehouseCode;

    /**
     * 商品序列号（SN码）
     */
    private String skuSnCode;

}
