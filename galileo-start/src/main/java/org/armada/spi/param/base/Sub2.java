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
public class Sub2 {

    private String sub2Field;

    private List<Sub3> sub2List;
}
