package org.armada.galileo.user.domain;

import lombok.Data;
import lombok.experimental.Accessors;
import org.armada.galileo.common.util.CommonUtil;
import org.armada.galileo.exception.BizException;

import java.util.Date;

/**
 * 用户信息
 */
@Data
@Accessors
public class UserInfoDomain {

    public void updatePwd(String newPwd) {
        if (CommonUtil.isEmpty(newPwd)) {
            throw new BizException("密码不能为空");
        }
        String salt = CommonUtil.getRandomNumber(4);
        String passowrd = CommonUtil.md5(CommonUtil.md5(newPwd) + salt);

        this.setSalt(salt);
        this.setPassword(passowrd);
    }

    public void checkPwd(String inputPwd) {
        String salt = this.getSalt();
        String compare = CommonUtil.md5(inputPwd + salt);

        if (!compare.equals(this.getPassword())) {
            throw new BizException("用户不存在或密码错误");
        }
    }

    //

    /**
     * 加密id
     */
    private String securityId;

    /**
     * 加密id
     */
    private String securityRoleId;


    /**
     * 主键
     */
    private Long id;

    /**
     * 登陆账号
     */
    private String loginId;

    /**
     * 密码
     */
    private String password;

    /**
     * 附加混淆
     */
    private String salt;

    /**
     * 姓名
     */
    private String name;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 手机号
     */
    private Long phone;

    /**
     * 所属角色id
     */
    private Long roleId;

    /**
     * 角色名
     */
    private String roleName;

    /**
     * 是否冻结 enable disable
     */
    private String status;

    /**
     * 用于模糊搜索的关键字
     */
    private String searchKeywords;

    /**
     * 头像路径
     */
    private String avatarUrl;

    /**
     * 入职时间
     */
    private Date hiredDate;

    /**
     * 员工工号
     */
    private String jobNumber;

    /**
     * 第三方预留id
     */
    private String openId;

}