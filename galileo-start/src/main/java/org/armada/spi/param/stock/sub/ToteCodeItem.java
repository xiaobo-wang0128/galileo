package org.armada.spi.param.stock.sub;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author xiaobo
 * @date 2021/4/26 11:41 上午
 */
@Data
@Accessors(chain = true)
public class ToteCodeItem {

    /**
     * 料箱编码 按料箱盘点时必填
     */
    private String binCode;
    /**
     * 格口码 若不传时，默认全料箱盘点
     */
    private String gridCode;



}
