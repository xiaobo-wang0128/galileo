package org.armada.galileo.open.vo;

import lombok.Data;
import lombok.experimental.Accessors;

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
public class OpenInterfaceConfigQueryVO {

    private static final long serialVersionUID = 1L;

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

}
