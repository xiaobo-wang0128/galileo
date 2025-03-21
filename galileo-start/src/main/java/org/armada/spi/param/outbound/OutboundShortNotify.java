package org.armada.spi.param.outbound;

import org.armada.spi.param.sku.SkuItem;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 商品缺货短拣通知
 *
 * @author xiaobo
 * @date 2021/4/22 5:38 下午
 */
@Data
@Accessors(chain = true)
public class OutboundShortNotify {

    /**
     * 外部流水号
     */
    private String taskId;

    /**
     * 补货出库单号
     */
    private String orderNo;

    /**
     * 发货单明细（二级，）
     */
    private List<SkuItem> skuList;

}
