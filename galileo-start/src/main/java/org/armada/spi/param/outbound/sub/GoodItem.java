package org.armada.spi.param.outbound.sub;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author xiaobo
 * @date 2021/5/10 11:17 下午
 */
@Data
@Accessors(chain = true)
public class GoodItem {

    /**
     * 商品条码
     */
    private String skuBarCode;

    /**
     * 拣选库位
     */
    private String locationCode;

    /**
     * 料箱格口
     */
    private String binCode;

    /**
     * 料箱编码
     */
    private String containerCode;

    /**
     * 实际拣选数量
     */
    private Integer qty;

    /**
     * 序列号
     */
    private List<String> itemList;


}
