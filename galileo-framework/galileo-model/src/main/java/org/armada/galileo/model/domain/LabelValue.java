package org.armada.galileo.model.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 通用前端对象
 * @author xiaobo
 * @date 2023/8/28 17:27
 */
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class LabelValue {

    /**
     * code
     */
    String code;

    /**
     * label
     */
    String label;

    /**
     * TEXT 文件 RADIO 单选
     */
    String type;

    /**
     * value
     */
    String value;

    /**
     * 可选项
     */
    List<String> options;
}
