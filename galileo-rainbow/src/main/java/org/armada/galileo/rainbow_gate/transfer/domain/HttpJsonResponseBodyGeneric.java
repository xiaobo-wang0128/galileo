package org.armada.galileo.rainbow_gate.transfer.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @author xiaobo
 * @date 2023/2/23 12:29
 */
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class HttpJsonResponseBodyGeneric<T> {

    private int code;
    private T data;
    private String errorCode;
    private String message;

}
