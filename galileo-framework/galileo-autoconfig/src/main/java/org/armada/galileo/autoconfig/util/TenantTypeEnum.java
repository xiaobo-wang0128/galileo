package org.armada.galileo.autoconfig.util;

/**
 * 租户类型
 *
 * @date 2024/1/12 14:11
 */
public enum TenantTypeEnum {

    /**
     * 租户/平台/贷代
     */
    TENANT,

    /**
     * 子机构
     */
    COMPANY,

    /**
     * 仓库（SCM 兼容）
     */
    WAREHOUSE,

    /**
     * 客户
     */
    CUSTOMER,

    /**
     * 登陆账号
     */
    USER,

}
