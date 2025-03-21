package org.armada.galileo.user.domain;


import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;
import java.util.List;

/**
 * 用户角色
 */
@Data
@Accessors
public class UserRoleDomain {

    /**
     * 加密id
     */
    private String securityId;

    /**
     * 角色id
     */
    private Long id;

    /**
     * 创建时间
     */
    private Date gmtCreate;

    /**
     * 创建人
     */
    private String creator;

    /**
     * 修改时间
     */
    private Date gmtModified;

    /**
     * 修改人
     */
    private String modifier;

    /**
     * 角色名
     */
    private String name;

    /**
     * 数据权限 code
     */
    private String dataPrivCode;

    /**
     * 权限菜单url集合
     */
    private List<String> menus;

    /**
     * 权限编码集合(按钮权限)
     */
    private List<String> btnCodes;

    /**
     * 允许的uri集合
     */
    private List<String> dataUris;

    /**
     * 备注说明
     */
    private String remark;
}