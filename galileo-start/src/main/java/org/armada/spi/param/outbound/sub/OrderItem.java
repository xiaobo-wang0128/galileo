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
public class OrderItem {

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 是否拣选完成
     * 0:全部拣选完成
     * 1:部分拣选完成
     * 2:全部未拣选
     * (是否增加一种状态： 因分箱导致的一个订单一次未拣选完成)
     */
    private String isComplete;

    /**
     * 波次号
     */
    private String waveCode;

    /**
     * 商品列表
     */
    private List<GoodItem> skuList;

}
