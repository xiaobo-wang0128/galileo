package org.armada.spi.param.stock;

import org.armada.spi.param.stock.sub.BinItem;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 盘点结果反馈
 *
 * @author xiaobo
 * @date 2021/4/22 6:27 下午
 */
@Data
@Accessors(chain = true)
public class StocktakeOrderResult {
    /**
     * 盘点单号
     */
    private String stocktakeCode;

    /**
     * 仓库编码
     */
    private String warehouseCode;

    /**
     * 状态
     */
    private String status;

    /**
     * 完成时间
     */
    private Long creationDate;

    /**
     * 操作台
     */
    private String stationCode;

    /**
     * 操作人
     */
    private String operate;

    /**
     * 盘点明细
     */
    private List<BinItem> binList;


}
