package org.armada.galileo.i18n_server.dal.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.armada.galileo.i18n_server.dal.enums.StatusEnum;
import org.armada.galileo.mybatis.domain.BaseEntity;

import java.io.Serializable;

/**
 * @author ake
 * @since 2021-12-21
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class I18nDictionaryValue extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * 应用主键
     */
    private Long appId;
    /**
     * 词条键值id
     */
    private Long dictionaryKeyId;
    /**
     * 词条对应值
     */
    private String dictionaryValue;
    /**
     * 词条对应语言
     */
    private String locale;

    /**
     * 原生枚举（带{@link StatusEnum}):
     * 启用或禁用->enable or disable
     */
    @TableField(fill = FieldFill.INSERT)
    private StatusEnum status;
}
