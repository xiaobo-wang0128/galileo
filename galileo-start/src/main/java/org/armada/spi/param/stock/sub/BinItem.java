package org.armada.spi.param.stock.sub;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author xiaobo
 * @date 2021/4/26 11:40 上午
 */
@Data
@Accessors(chain = true)
public class BinItem {

    /**
     * 料箱格口
     */
    private String gridCode;

    /**
     * 料箱编码
     */
    private String binCode;

    /**
     * 商品条码
     */
    private String skuBarCode;

    /**
     * 系统数量
     */
    private String inStockQty;

    /**
     * 盘点数量
     */
    private String stocktakeQty;

    /**
     * 库位条码
     */
    private String locationCode;

    /**
     * snList
     */
    private List<String> itemList;

}
