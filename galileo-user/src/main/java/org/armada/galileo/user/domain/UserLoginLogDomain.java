package org.armada.galileo.user.domain;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * 用户登陆日志
 *
 * @author xiaobo
 * @date 2022/2/7 12:43 下午
 */
@Data
@Accessors(chain = true)
public class UserLoginLogDomain {

    /**
     * 主键
     */
    private Long id;

    /**
     * 类型 login 登陆, logout 登出
     */
    private String type;

    /**
     * 登陆登出时间
     */
    private Date happenTime;

    /**
     * 登陆 ip
     */
    private String loginIp;

    /**
     * 登陆账号
     */
    private String loginId;

    /**
     * 姓名
     */
    private String name;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 所属角色id
     */
    private Long roleId;

    /**
     * 角色名
     */
    private String roleName;

    /**
     * 备注信息
     */
    private String remark;

    /**
     * 用于模糊搜索的关键字
     */
    private String searchKeywords;

}
