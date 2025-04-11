package org.armada.galileo.i18n_server.xunfei_api;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author xiaobo
 * @date 2023/9/6 20:06
 */
@Data
@Accessors(chain = true)
public class XunFeiConfig {

    // 应用ID（到控制台获取）
    private String appId = "f46df18f";

    // 接口APISercet（到控制台机器翻译服务页面获取）
    private String appSecret = "OTNjMTg0MmFiOWZmNjJhNzVjOThhNWNm";

    // 接口APIKey（到控制台机器翻译服务页面获取）
    private String apiKey = "eb5a375478509ab8c82052c8fcc5b7a5";

}
