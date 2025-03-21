package org.armada.galileo.i18n_server.dal.dto;

import lombok.Data;
import org.armada.galileo.i18n_server.dal.enums.StatusEnum;

import java.util.Date;
import java.util.List;

/**
 * @author ake
 * @since 2021-12-21
 */
@Data
public class I18nAppDTO {
    /**
     * 主键
     */
    private Integer id;
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
    private List<String> locales;

    /**
     * 资源文件导出类型
     */
    private String exportType;

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
