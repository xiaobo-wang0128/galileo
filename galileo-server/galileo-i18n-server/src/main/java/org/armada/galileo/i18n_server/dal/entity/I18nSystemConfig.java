package org.armada.galileo.i18n_server.dal.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.armada.galileo.mybatis.handler.ListStringTypeHandler;

import java.io.Serializable;
import java.util.List;

/**
 * @author ake
 * @since 2021-12-21
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName(autoResultMap = true)
public class I18nSystemConfig extends BaseMysqlEntity implements Serializable  {

    private static final long serialVersionUID = 1L;
    /**
     * 应用code
     */
    private String configKey;
    /**
     * 应用名称
     */
    private String configValue;

}
