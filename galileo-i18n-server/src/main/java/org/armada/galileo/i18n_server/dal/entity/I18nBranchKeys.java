package org.armada.galileo.i18n_server.dal.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.apache.ibatis.annotations.ResultMap;

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
public class I18nBranchKeys extends BaseMysqlEntity implements Serializable  {

    private static final long serialVersionUID = 1L;
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
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Set<String> dictionaryKeys;

}
