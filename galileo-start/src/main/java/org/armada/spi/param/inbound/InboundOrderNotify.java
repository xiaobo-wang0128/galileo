package org.armada.spi.param.inbound;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 上架任务
 *
 * @author xiaobo
 * @date 2021/4/22 4:23 下午
 */
@Data
@Accessors(chain = true)
public class InboundOrderNotify {

    /**
     * 原任务号
     */
    private String sourceTaskID;

    /**
     * 仓库编码
     */
    private String warehouseCode;

    /**
     * 料箱号 容器号（即pallet_code）
     */
    private String binCode;

    /**
     * 上架完成时间
     */
    private Long finishTime;

    /**
     * 上架机器人编码	是一个编码值，QT不给
     */
    private String robotCode;

    /**
     * 上架入库口	入库接驳口口位置编码
     */
    private String inStationLocCode;

    /**
     * 上架库位 货架库位条码
     */
    private String locationCode;


//    private Long putawayTime;

}
