package org.armada.spi.param.stock.sub;

import org.armada.spi.param.sku.SkuItem;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 盘点单
 * @author xiaobo
 * @date 2021/4/23 2:56 下午
 */
@Data
@Accessors(chain = true)
public class StocktakeOrder {

    /**
     * 盘点单任务号
     */
    private String taskId;

    /**
     * 仓库仓库编码
     */
    private String warehouseCode;

    /**
     * 客户盘点指令盘点单号
     */
    private String stocktakeCommandCode;

    /**
     * 指令类型
     * 2 按商品盘点，商品列表必填
     * 3 按料箱盘点
     */
    private String type;

    /**
     * 盘点方式
     * 0 明盘
     * 1 盲盘
     * (默认0)
     */
    private String stocktakeCommandType;

    /**
     * 盘点类型
     * 0 初盘
     * 1 复盘
     * 2 异常盘
     */
    private String stocktakeType;

    /**
     * 业务盘点优先级	枚举值，
     */
    private String priority;

    /**
     * 创建时间	盘点单创建时间，UTC时间戳
     */
    private Long creationDate;

    /**
     * 盘点商品信息
     */
    private List<SkuItem> skuList;

    /**
     * 盘点料箱信息
     */
    private List<ToteCodeItem> toteCodeList;


}
