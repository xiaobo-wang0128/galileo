package org.armada.spi.param.stock.sub;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 变动明细
 * @author xiaobo
 * @date 2021/4/26 11:40 上午
 */
@Data
@Accessors(chain = true)
public class ChangeDetail {

    /**
     * 原料箱编码
     */
    private String fromBinCode;

    /**
     * 原料箱格口编码
     */
    private String fromGridCode;

    /**
     * 目标料箱编码
     */
    private String toBinCode;

    /**
     * 目标箱格口编码
     */
    private String toGridCode;

    /**
     * 商品条码
     */
    private String skuBarCode;

    /**
     * 货主
     */
    private String ownerCode;

    /**
     * 序列号
     */
    private String itemSerno;

}
