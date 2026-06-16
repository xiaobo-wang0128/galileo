package org.armada.galileo.model.domain;


import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 当前登录用户（存储在 cookie 中, 不能加太多字段）
 */
@Data
@Accessors(chain = true)
public class LoginUser {

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

//    /**
//     * 当前用户所属子机构code
//     */
//
//    private String companyCode;

    /**
     * 当前用户所属子机构名称
     */
    private String companyName;
//
//    /**
//     * 当前用户的 仓库id
//     */
//
//    private Long currentWarehouseId;
//
//    /**
//     * 客户 - 当前客户的 companyId
//     */
//
//    private Long customerId;

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
