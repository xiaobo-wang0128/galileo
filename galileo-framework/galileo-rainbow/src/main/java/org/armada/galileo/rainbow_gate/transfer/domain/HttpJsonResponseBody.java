package org.armada.galileo.rainbow_gate.transfer.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author xiaobo
 * @date 2023/2/22 17:22
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class HttpJsonResponseBody {
    private int code;
    private Object data;
    private String errorCode;
    private String message;
}
