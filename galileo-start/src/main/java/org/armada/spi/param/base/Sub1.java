package org.armada.spi.param.base;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author xiaobo
 * @date 2022/1/13 9:56 上午
 */
@Data
@Accessors(chain = true)
public class Sub1 {
    private String sub1Field;

    private List<Sub2> sub2List;
}
