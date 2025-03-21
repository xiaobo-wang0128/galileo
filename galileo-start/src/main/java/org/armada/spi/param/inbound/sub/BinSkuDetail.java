package org.armada.spi.param.inbound.sub;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 格口中的商品信息
 *
 * @author xiaobo
 * @date 2021/4/26 11:53 上午
 */

@Data
@Accessors(chain = true)
public class BinSkuDetail {

    /**
     * 料格编号
     */
    private String gridCode;

    /**
     * 货主编码
     */
    private String customerCode;

    /**
     * 货品编码  举例：M0001XXXXX
     */
    private String skuCode;

    /**
     * 是否命中SKU团聚  0 不团聚;1 团聚
     */
    private String isHit;

    /**
     * 商品管理方式  0：序列号管理;1：非序列号管理 (默认0)
     */
    private String manageType;

    /**
     * 已有单品数量
     */
    private String itemQty;

    /**
     * 推荐上架单品数量  isHit为1，此处必须大于0
     */
    private String recommendItemQty;

    /**
     * sn列表（三级 )
     */
    private List<String> itemList;



}
