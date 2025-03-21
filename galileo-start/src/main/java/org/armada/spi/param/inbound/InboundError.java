package org.armada.spi.param.inbound;

import org.armada.spi.param.sku.SkuInfo;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 入库取箱异常通知
 *
 * @author xiaobo
 * @date 2021/4/22 5:04 下午
 */
@Data
@Accessors(chain = true)
public class InboundError {

    /**
     * 箱子编码
     */
    private String binCode;

    /**
     * 取箱位置
     */
    private String locCode;

    /**
     * 异常原因
     */
    private String errMessage;

    /**
     * sku 列表
     */
    private List<SkuInfo> skuInfoList;

}
