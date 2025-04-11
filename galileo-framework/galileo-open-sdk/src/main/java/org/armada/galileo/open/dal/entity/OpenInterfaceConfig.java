package org.armada.galileo.open.dal.entity;

import lombok.Data;
import lombok.experimental.Accessors;
import org.armada.galileo.annotation.openapi.ApiRole;
import org.armada.galileo.es.entity.EsBaseEntity;

/**
 * <p>
 * 接口参数转换配置信息
 * </p>
 *
 * @author
 * @since 2023-02-03
 */
@Data
@Accessors(chain = true)
public class OpenInterfaceConfig implements EsBaseEntity {

    @Override
    public String getIdValue() {
        return id;
    }

    private String id;

    /**
     * 接口唯一标识（地址）
     */
    private String apiUrl;

    /**
     * 开发者账号id
     */
    private Long accountId;

    /**
     * 应用id
     */
    private Long appId;

    /**
     * 回传地址
     */
    private String httpUrl;

    /**
     * 调用方
     */
    private ApiRole apiFrom;

    /**
     * 被调用方
     */
    private ApiRole apiTo;

    /**
     * 输入参数转换
     */
    private String input;

    /**
     * 输出参数转换
     */
    private String output;


}
