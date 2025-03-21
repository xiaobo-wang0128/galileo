package org.armada.spi.param.stock.sub;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author xiaobo
 * @date 2021/4/26 11:40 上午
 */
@Data
@Accessors(chain = true)
public class AdjustItem {

    /**
     * 料箱编码
     */
    private String binCode;

    /**
     * 格口编码
     */
    private String gridCode;

    /**
     * 货主
     */
    private String customerCode;

    /**
     * 商品条码	商品条码
     */
    private String skuBarCode;

    /**
     * 商品等级	0：正  1：残
     */
    private String qualityLevel;

    /**
     * 调整单调整商品数量
     */
    private Integer qty;

    /**
     * sn码列表 "普通商品非必填，SN管理商品必填"
     */
    private List<String> itemList;


}
