package org.armada.spi.param.outbound;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * sku 库存丢失通知
 *
 * @author xiaobo
 * @date 2021/4/22 6:13 下午
 */

@Data
@Accessors(chain = true)
public class OutboundSkuLossNotify {

    /**
     * 外部流水号
     */
    private String taskId;

    /**
     * 商品条码
     */
    private String skuBarCode;

    /**
     * 物权所有者	货主
     */
    private String customerCode;

    /**
     * 丢失类型 0：拣选丢失，1：移库丢失
     */
    private Integer lostType;

    /**
     * 料箱格口	料箱格口
     */
    private String binCode;

    /**
     * 料箱编码
     */
    private String gridSerno;

    /**
     * 料箱格丢失数量	丢失数量
     */
    private Integer lostQty;

    /**
     * 料箱格丢失sn列表，没有就是空list
     */
    private List<String> snList;

    /**
     * 操作站id
     */
    private Integer stationId;

    /**
     * 操作员名称
     */
    private String operator;


}
