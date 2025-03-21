package org.armada.galileo.user.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 数据权限定义
 *
 * @author xiaobo
 * @date 2022/2/9 2:42 下午
 */
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class DataPrivDomain {

    /**
     * 数据权限名称
     */
    private String name;

    /**
     * 数据权限描述
     */
    private String desc;

    /**
     * 编码
     */
    private String code;

}
