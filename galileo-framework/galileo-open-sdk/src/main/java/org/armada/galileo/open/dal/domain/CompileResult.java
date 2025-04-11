package org.armada.galileo.open.dal.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author xiaobo
 * @date 2023/2/3 17:30
 */

@Data
@Accessors(chain = true)
@AllArgsConstructor
public class CompileResult implements Serializable {

    /**
     * cls
     */
    private String clsName;

    /**
     * 是否内部类
     */
    private Boolean isInner = false;

    /**
     * 字节码 base64
     */
    private byte[] classBytes;

}