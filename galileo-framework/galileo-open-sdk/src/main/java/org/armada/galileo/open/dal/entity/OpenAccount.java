package org.armada.galileo.open.dal.entity;

import lombok.Data;
import lombok.experimental.Accessors;
import org.armada.galileo.es.entity.EsBaseEntity;

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
public class OpenAccount implements EsBaseEntity {

    @Override
    public String getIdValue() {
        return id;
    }

    /**
     * 主键id
     */
    private String id;

    /**
     * 登陆账号
     */
    private String loginId;

    /**
     * 密码
     */
    private String password;

    /**
     * 密码混淆
     */
    private String salt;

    /**
     * 姓名
     */
    private String name;

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
