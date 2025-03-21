package org.armada.spi.param.stock;

import org.armada.spi.param.stock.sub.GoodItem;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 料箱格口库存信息（用于定时库存对账）
 *
 * @author xiaobo
 * @date 2021/4/22 7:09 下午
 */

@Accessors(chain = true)
@Data
public class StockOfTote {

    /**
     * 仓库编码
     */
    private String warehouseCode;

    /**
     * 容器号（ 即pallet_code）料箱号
     */
    private String binCode;

    /**
     * 库位位置编码（回传一个统一值，Robot为一个大库位）	库位
     */
    private String locationCode;

    /**
     * 格口编码
     */
    private String gridCode;

    /**
     * 格口种类，参数格式: n_n 表示格口布局为n行n列
     * 例， 1_1：一分格， 1_2：二分格， 2_2：四分格， 2_4：八分格
     */
    private String gridType;

    /**
     * 格口中的商品列表
     */
    private List<GoodItem> skuList;


}
