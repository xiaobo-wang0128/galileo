package org.armada.galileo.mybatis.domain;

/**
 * @author xiaobo
 *
 * @date 2023/4/17 13:57
 */
public enum TenantTableCheckType {

    /**
     * 黑名单方式， 配置不需要租户过滤的表
     */
    BLANK,

    /**
     * 白名单方式， 配置需要租户过滤的表
     */
    WHITE
}
