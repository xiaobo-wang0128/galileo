package org.armada.galileo.i18n_server.dal.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
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
public class I18nApp extends BaseMysqlEntity implements Serializable  {

    private static final long serialVersionUID = 1L;
    /**
     * 应用code
     */
    private String appCode;
    /**
     * 应用名称
     */
    private String appName;
    /**
     * 应用描述
     */
    private String appDescribe;
    /**
     * 应用包含的语言
     */
    @TableField(typeHandler = ListStringTypeHandler.class)
    private List<String> locales;

    /**
     * 资源文件导出类型 js java
     */
    private String exportType;

}
