package org.armada.galileo.i18n_server.dal.dto;

import lombok.Data;
import org.armada.galileo.i18n_server.dal.enums.StatusEnum;

import java.util.Date;

/**
 * @author ake
 * @since 2021-12-21
 */
@Data
public class I18nDictionaryValueDTO {
    /**
     * 主键
     */
    private Integer id;
    private static final long serialVersionUID = 1L;
    /**
     * 应用主键
     */
    private Integer appId;
    /**
     * 词条键值id
     */
    private Integer dictionaryKeyId;
    /**
     * 词条对应值
     */
    private String dictionaryValue;
    /**
     * 词条对应语言
     */
    private String locale;
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
