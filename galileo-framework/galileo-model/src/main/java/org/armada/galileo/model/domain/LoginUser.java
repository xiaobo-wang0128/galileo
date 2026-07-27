package org.armada.galileo.model.domain;


import lombok.Data;
import lombok.experimental.Accessors;
import org.armada.galileo.model.constant.RoleScopeEnum;
import org.armada.galileo.model.constant.UserTypeEnum;

/**
 * 当前登录用户（存储在 cookie 中, 不能加太多字段）
 */
@Data
@Accessors(chain = true)
public class LoginUser {

    /**
     * 当前用户是否为'平台管理员'
     *
     * @return
     */
    public boolean isSuperAdmin() {
        return roleScope == RoleScopeEnum.DEFAULT; //&& userRoleType == RoleTypeEnum.ADMIN;
    }

    /**
     * 当前用户是否为'机构'
     *
     * @return
     */
    public boolean isCompany() {
        return roleScope == RoleScopeEnum.COMPANY && userType == UserTypeEnum.COMPANY;
    }

    /**
     * 当前用户是否为客户
     *
     * @return
     */
    public boolean isCustomer() {
        return userType == UserTypeEnum.CUSTOMER;
    }

    /**
     * 当前用户组织类型: 供应商、机构、客户
     */
    private UserTypeEnum userType;

    /**
     * 当前用户角色有效范围：全局默认角色、公司角色
     */
    private RoleScopeEnum roleScope;

    /**
     * 用户Id
     */
    private Long userId;

    /**
     * 用户代码（用户登录账号，等于login_id）
     */
    private String loginId;

    /**
     * 用户名称
     */
    private String userName;

    /**
     * 当前用户/客户的机构id
     */
    private Long tenantId;

    /**
     * 当前用户所属子机构id
     */
    private Long companyId;

    /**
     * 当前用户所属子机构名称
     */
    private String companyName;

    /**
     * 头像路径
     */
    private String avatarUrl;

    /**
     * 第三方平台用户： 当前用户所属平台编号
     */

    private String platformCode;

    /**
     * 第三方平台用户： 当前用户所属平台名称
     */

    private String platformName;

    /**
     * 客户在支付平台上的开放id
     */
    private String openId;

    /**
     * 应用id
     */
    private String appId;

    /**
     * 客户电话 - 微信
     */
    private String mobile;

    /**
     * 普查队伍id
     */
    private Long teamId;

    /**
     * 普查队伍名称
     */
    private String teamName;

    /**
     * 随机key 用于 websocket连接
     */
    private Long wsKey;

    /**
     * 当前用户所在部门id
     */
    private Long deptId;

    /**
     * 当前用户所在部门名称
     */
    private String deptName;

}
