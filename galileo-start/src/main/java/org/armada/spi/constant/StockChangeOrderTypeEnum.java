package org.armada.spi.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Jingtang
 * @date 2021/6/15 14:14
 * @Description:
 */
@Getter
@AllArgsConstructor
public enum StockChangeOrderTypeEnum {

    // 单据类型
    REORG(1, "理库"),
    COUNTING(2, "盘点"),
    PICKING(3, "拣货")

    ;

    private final int code;

    private final String name;


}
