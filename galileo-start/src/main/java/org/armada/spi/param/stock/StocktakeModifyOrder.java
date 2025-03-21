package org.armada.spi.param.stock;

import org.armada.spi.param.stock.sub.AdjustItem;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 盘点差异调整单下发，根据该订单调整料箱格口库存
 * @author xiaobo
 * @date 2021/4/23 3:05 下午
 */
@Data
@Accessors(chain = true)
public class StocktakeModifyOrder {

    /**
     * 仓库
     */
    private String warehouseCode;

    /**
     * 客户调整单号
     */
    private String outAdjustmentCode;

    /**
     * 外部调整类型（0：找回指的是盘盈，1：丢失指的是从该库位盘亏；）
     */
    private String outAdjustmentType;

    /**
     * 创建时间
     */
    private Long creationDate;

    /**
     * 备注
     */
    private String remark;

    /**
     * 调整单明细（二级，adjustmentList）
     */
    private List<AdjustItem> adjustmentList;


}
