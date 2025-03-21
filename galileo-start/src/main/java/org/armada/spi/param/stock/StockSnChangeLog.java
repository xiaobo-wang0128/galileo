package org.armada.spi.param.stock;

import org.armada.spi.param.stock.sub.ChangeDetail;
import lombok.Data;
import lombok.experimental.Accessors;
import org.armada.spi.constant.StockChangeOrderTypeEnum;

import java.util.List;

/**
 * SN库存变动记录
 *
 * @author xiaobo
 * @date 2021/4/23 3:35 下午
 */
@Data
@Accessors(chain = true)
public class StockSnChangeLog {

    /**
     * 变动流水号 该条记录的唯一标识
     */
    private String changeLogId;

    /**
     * 单据类型
     * 1 理库
     * 2 盘点
     * 3 拣货出库
     * @see StockChangeOrderTypeEnum
     */
    private Integer orderType;

    /**
     * 变动单号
     */
    private String changeOrderCode;

    /**
     * 变动列表
     */
    private List<ChangeDetail> changeList;


}
