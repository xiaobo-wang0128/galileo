package org.armada.spi.param.outbound.sub;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author Jingtang
 * @date 2021/5/19 11:04
 * @Description:
 */
@Data
@Accessors(chain = true)
public class OutboundOrderDetail {

    /**
     * 商品条码
     */
    private String merchandiseSerno;

    /**
     * 物权所有者
     */
    private String customerCode;

    /**
     * 数量
     */
    private Integer qty;

    /**
     * 0: 序列号管理
     * 1: 非序列号管理
     * 默认0
     */
    private Integer manageType = 0;

    /**
     * 0: 正
     * 1: 残
     */
    private Integer qualityLevel = 0;

    /**
     * 业务类型：2B 2C
     * 默认2C
     */
    private String businessType = "2C";



}
