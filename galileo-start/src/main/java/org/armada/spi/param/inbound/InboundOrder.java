package org.armada.spi.param.inbound;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 上架任务下发（入库单）
 *
 * @author xiaobo
 * @date 2021/4/23 11:00 上午
 */
@Data
@Accessors(chain = true)
public class InboundOrder {

    /**
     * 入库任务号，唯一入库任务号
     */
    private String taskId;

    /**
     * 仓库编码	仓库
     */
    private String warehouseCode;

    /**
     * 料箱编码	料箱编码
     */
    private String binCode;

    /**
     * 客户的入库订单号，重复	客户订单号
     */
    private String orderNo;

    /**
     * 接入方收货单创建时间，UTC时间戳, 默认为系统当前时间	下发时间
     */
    private Long operationTime = System.currentTimeMillis();

    /**
     * 操作模式
     * 0 输送线协同上架
     * 1 AGV整箱直接上架
     * 默认为 0 （仅快仓需要使用）
     */
    private Integer operationMode;

    /**
     * 入库单明细（二级，grideList，按SKU、批次号、容器汇总）
     */
    private List<InboundOrderDetail> gridList;

}
