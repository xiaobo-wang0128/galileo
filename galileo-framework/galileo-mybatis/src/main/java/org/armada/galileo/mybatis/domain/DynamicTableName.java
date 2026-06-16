package org.armada.galileo.mybatis.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * mybatis 动态表名
 *
 * @author xiaobo
 * @date 2023/8/28 10:54
 */
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class DynamicTableName {

    /**
     * 原表名
     */
    private String sourceTableName;

    /**
     * 真实表名
     */
    private String targettableName;
}
