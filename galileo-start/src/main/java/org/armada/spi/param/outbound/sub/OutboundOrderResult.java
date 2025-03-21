package org.armada.spi.param.outbound.sub;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 出库单（发货单）回传（按箱）
 *
 * @author xiaobo
 * @date 2021/4/22 5:28 下午
 */

@Data
@Accessors(chain = true)
public class OutboundOrderResult {

    /**
     * 容器编码
     */
    private String containerCode;

    /**
     * 订单清单
     */
    private List<OrderItem> orderList;

}
