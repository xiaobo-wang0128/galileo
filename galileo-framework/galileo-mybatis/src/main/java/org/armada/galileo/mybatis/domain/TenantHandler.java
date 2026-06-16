package org.armada.galileo.mybatis.domain;


import org.armada.galileo.model.domain.Pair;

import java.util.List;
import java.util.Map;

/**
 * @author xiaobo
 * @date 2023/4/17 11:47
 */
public interface TenantHandler {
    /**
     * 表名配置, 白名单方式：配置需要租户过滤的表， 黑名单：配置不需要过滤的表
     * key 为 租户字段名, value 为 表名
     * 用于表达哪些表有哪些租户字段， 部分表会存在多个维度的租户字段
     *
     * @return
     */
    public Map<String[], String[]> getTables();

    /**
     * 返回租户隔离字段名，字段值
     *
     * @return
     */
    public List<Pair<String, Long>> getTenantColumnMap();

}
