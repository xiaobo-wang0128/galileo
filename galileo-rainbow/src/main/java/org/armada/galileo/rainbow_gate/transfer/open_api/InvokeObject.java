package org.armada.galileo.rainbow_gate.transfer.open_api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.lang.reflect.Method;

/**
 * @author xiaobo
 * @date 2023/4/19 20:16
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class InvokeObject {
    private Method method;
    private String[] paramNames;
    private Object target;
}