package org.armada.spi.param.inbound;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 空料箱、空格口出库（空箱、空格口信息下发）
 *
 * @author xiaobo
 * @date 2021/4/23 10:55 上午
 */
@Data
@Accessors(chain = true)
public class ToteMoveOutOrder {

    /**
     * 订单任务号
     */
    private String taskId;

    /**
     * 拆包台目的地
     */
    private String unpackingStation;

    /**
     * 叫料箱类型
     * 0:空料箱
     * 1:料箱空格
     * 2:SKU团聚
     * 3:料箱编码
     */
    private String binType;

    /**
     * 申请料格种类，参数格式: n_n 表示格口布局为n行n列
     * 例， 1_1：一分格， 1_2：二分格， 2_2：四分格， 2_4：八分格
     */
    private String emptyGridType;

    /**
     * 料箱数量
     */
    private Integer binQty;

    /**
     * 料箱编码
     */
    private String binCode;

    /**
     * 商品条码
     */
    private String skuBarCode;

    /**
     * 待入库体积
     */
    private Double volume;

    /**
     * 长
     */
    private Double length;

    /**
     * 宽
     */
    private Double width;

    /**
     * 高
     */
    private Double height;

    /**
     * 重量
     */
    private Double weight;

}
