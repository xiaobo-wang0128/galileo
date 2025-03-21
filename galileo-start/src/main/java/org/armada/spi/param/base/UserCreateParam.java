package org.armada.spi.param.base;

import org.armada.spi.annotation.Validation;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;

/**
 * 用户创建参数
 *
 * @author xiaobo
 * @date 2021/4/23 10:22 上午
 */
@Data
@Accessors(chain = true)
public class UserCreateParam {

    /**
     * 用户账号	登录账号，用户唯一标识
     */
    @Validation
    private String userAccount;

    /**
     * 用户昵称	"登录后界面显示得昵称
     * 默认：登录账号"
     */
    private String userNickname;

    /**
     * 用户密码
     */
    @Validation
    private String userPassword;

    /**
     * 用户状态	"0:启用 1:停用删除：停用状态"
     */
    @Validation
    private String userStatus;

    /**
     * 角色权限类型
     * 1:超级管理员，
     * 2:操作员
     * 3:运维人员
     * 4:管理员
     */
    private String roleType;

    /**
     * 创建人
     */
    private String createdBy;


    /**
     * map test
     */
    private Map<String,Object> map;

    /**
     * test
     */
    @Validation
    private List<Sub1> sub1List;




}
