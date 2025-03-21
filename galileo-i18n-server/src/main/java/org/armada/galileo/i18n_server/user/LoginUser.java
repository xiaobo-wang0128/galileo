package org.armada.galileo.i18n_server.user;


import lombok.Data;
import lombok.experimental.Accessors;


@Data
@Accessors(chain = true)
public class LoginUser {

    /**
     * 用户Id
     */
    private Long userId;

    /**
     * 姓名
     */
    private String name;

//    /**
//     * 昵称
//     */
//    private String nickname;

    /**
     * 手机号
     */
    private Long mobile;

    /**
     * 角色Id
     */
    private Long roleId;

    /**
     * 角色类型
     */
    private String dataRoleCode;

    /**
     * 头像路径
     */
    private String avatarUrl;

}
