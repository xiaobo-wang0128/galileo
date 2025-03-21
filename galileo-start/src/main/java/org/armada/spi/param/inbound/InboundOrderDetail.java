package org.armada.spi.param.inbound;


import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 上架任务下发（入库单明细）
 */
@Data
@Accessors(chain = true)
public class InboundOrderDetail {

    /**
     * 格口编码
     */
    private String grideSerno;

    /**
     * 物权所有者 - 货主
     */
    private String customerCode;

    /**
     *商品条码
     */
    private String merchandiseSerno;

    /**
     *商品名称
     */
    private String  merchandiseName;

    /**
     * 数量
     */
    private Integer qty;

    /**
     * 商品品质   0：正  1：残 默认为0
     */
    private Integer qualityLevel;

    /**
     * 商品管理方式  0：序列号管理；1：非序列号管理 （默认0）
     */
    private Integer manageType;

    /**
     * 业务类型：2B,2C 默认2C
     */
    private String businessType;

    /**
     * 生产日期，UTC时间戳
     */
    private Long productionDate;

    /**
     * 到期日期，UTC时间戳
     */
    private Long expirationDate;

    /**
     * sn条码集合   序列号，Robot不做校验
     */
    private List<String> itemList;

}
