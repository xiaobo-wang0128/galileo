package org.armada.galileo.i18n_server.dal.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.armada.galileo.i18n_server.dal.enums.StatusEnum;
import org.armada.galileo.mybatis.domain.BaseEntity;

import java.io.Serializable;
import java.util.Set;

/**
 * @author ake
 * @since 2022-04-21
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName(autoResultMap = true)
public class I18nBranchKeys extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * 应用主键
     */
    private Long appId;
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
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Set<String> dictionaryKeys;

    /**
     * 原生枚举（带{@link StatusEnum}):
     * 启用或禁用->enable or disable
     */
    @TableField(fill = FieldFill.INSERT)
    private StatusEnum status;

}
