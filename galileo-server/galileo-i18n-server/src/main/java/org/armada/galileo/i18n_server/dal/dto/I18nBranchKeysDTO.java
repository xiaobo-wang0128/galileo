package org.armada.galileo.i18n_server.dal.dto;

import lombok.Data;
import org.armada.galileo.i18n_server.dal.enums.StatusEnum;

import java.util.Date;
import java.util.Set;

/**
 * @author ake
 * @since 2021-12-21
 */
@Data
public class I18nBranchKeysDTO {
    /**
     * 应用主键
     */
    private Integer appId;
    /**
     * 分支类型
     */
    private String branchType;
    /**
     * 分支名称
     */
    private String branchPath;
    /**
     * 分支包含的keys
     */
    private Set<String> dictionaryKeys;
    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 修改时间
     */
    private Date updateTime;
    /**
     * 创建人
     */
    private String createUser;
    /**
     * 修改人
     */
    private String updateUser;
    /**
     * 创建人id
     */
    private Long createUserId;
    /**
     * 修改人id
     */
    private Long updateUserId;
    /**
     * 原生枚举（带{@link StatusEnum}):
     * 启用或禁用->enable or disable
     */
    private StatusEnum status;

}
