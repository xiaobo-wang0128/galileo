package org.armada.spi.param.stock.sub;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author xiaobo
 * @date 2021/4/26 11:39 上午
 */

@Data
@Accessors(chain = true)
public class GoodItem {

    /**
     * 商品条码
     */
    private String skuBarCode;

    /**
     * 物权所有者	货主
     */
    private String customerCode;

    /**
     * 数量
     */
    private String amount;

    /**
     * 是否是序列号商品  0：不是 1：是	是否是序列号商品
     */
    private String isSequenceSku;

    /**
     * 商品等级 0：正  1：残
     */
    private String skuLevel;

    /**
     * sn 列表
     */
    private List<String> snCodeList;


}
