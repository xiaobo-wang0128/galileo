package org.armada.galileo.model.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @author xiaobo
 * @date 2025/2/24 18:11
 */
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class KeyValue {

    String label;

    Object value;

}
