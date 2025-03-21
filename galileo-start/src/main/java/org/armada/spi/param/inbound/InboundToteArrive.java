package org.armada.spi.param.inbound;

import org.armada.spi.param.inbound.sub.BinSkuDetail;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 入库 - 料箱到达信息通知
 *
 * @author xiaobo
 * @date 2021/4/22 5:10 下午
 */

@Data
@Accessors(chain = true)
public class InboundToteArrive {

    /**
     * 原申请任务号 空格口信息下发时候的taskId
     */
    private String sourceTaskId;

    /**
     * 拆包台编号	一条目标输送线代表一个拆包台
     */
    private String unpackingStaionCode;

    /**
     * 申请料格种类，参数格式: n_n 表示格口布局为n行n列
     * 例， 1_1：一分格， 1_2：二分格， 2_2：四分格， 2_4：八分格
     */
    private String containerType;

    /**
     * 料箱编号
     */
    private String containerCode;

    /**
     * 料箱中的商品明细
     */
    private List<BinSkuDetail> binSkuDetailList;

}
