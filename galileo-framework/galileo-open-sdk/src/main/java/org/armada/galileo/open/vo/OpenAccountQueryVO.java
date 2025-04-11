package org.armada.galileo.open.vo;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * <p>
 * 开放平台开发者账号信息
 * </p>
 *
 * @author 
 * @since 2023-02-03
 */
@Data
@Accessors(chain = true)
public class OpenAccountQueryVO {

    private static final long serialVersionUID = 1L;

    /**
     * 登陆账号
     */
    private String loginId;

    /**
     * 手机号
     */
    private String mobile;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 最后登录时间
     */
    private Long lastLoginTime;


}
