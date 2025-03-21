package org.armada.spi.param.outbound;

import org.armada.spi.param.outbound.sub.OutboundOrderDetail;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 出库单下发信息
 *
 * @author xiaobo
 * @date 2021/4/23 2:45 下午
 */
@Data
@Accessors(chain = true)
public class OutboundOrder {


    /**
     * 出库订单ID，唯一
     */
    private String orderId;

    /**
     * 出库订单号	客户发货单号
     */
    private String orderNo;

    /**
     * 仓库	仓库编码
     */
    private String warehouseCode;

    /**
     * 货主	物权所有者
     */
    private String customerCode;

    /**
     * 波次号
     * 使用，传拣选方式分组（Robot是否需要用于分组判断？）
     */
    private String outWaveCode;

    /**
     * 源容器
     */
    private String origContainerSerno;

    /**
     * 订单类型
     * 0：标准出库
     * 1：销毁出库
     * 2：自提出库
     * 3：增值出库
     */
    private Integer orderType;

    /**
     * 是否加包装
     */
    private Integer isAddPackage;

    /**
     * 拣选类型	"0：单一拣选
     * 1：复合拣选（1sku）
     * 2：复合拣选（多sku）
     * 3、合单拣选
     * 4、补拣"
     */
    private Integer pickType;

    /**
     * 组波单量限制 "复合订单（1sku)与复合订单（多sku）才必填， 且复合订单（多sku）内的合单传数量为1"
     */
    private Integer waveNo;

    /**
     * 订单指定容器类型
     * 1：拣货车
     * 2：周转箱
     * 默认1
     */
    private Integer containerType = 1;

    /**
     * 是否允许拆箱
     * 0：禁止
     * 1：允许
     * 默认 0 （是否允许拆箱由谁判断）
     */
    private Integer isAllowSplit = 0;

    /**
     * 是否上分拣线
     * 0：不上分拣线
     * 1：上分拣线
     * 默认 0
     */
    private Integer isLine = 0;

    /**
     * 优先级（数据越大优先级越高）
     * 0：普通
     * 1：紧急
     * 2：加急
     * 9：特急
     * 默认 0
     */
    private Integer priority = 0;

    /**
     * 截单时间，UTC时间戳。不传系统将默认截单时间
     */
    private Long expectedFinishDate;

    /**
     * 发货单明细
     */
    private List<OutboundOrderDetail> skuList;

}
