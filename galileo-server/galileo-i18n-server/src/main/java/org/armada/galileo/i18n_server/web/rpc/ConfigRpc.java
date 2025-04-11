package org.armada.galileo.i18n_server.web.rpc;

import org.armada.galileo.i18n_server.service.I18nSystemConfigService;
import org.armada.galileo.i18n_server.xunfei_api.XunFeiConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author xiaobo
 * @date 2023/9/6 20:05
 */
@Controller
public class ConfigRpc {

    @Autowired
    private I18nSystemConfigService configService;

    public void updateXunFeiConfig(@RequestBody XunFeiConfig cfg) {
        configService.updateXunFeiConfig(cfg);
    }

    public XunFeiConfig checkXunFeiConfg() {
        return configService.checkXunFeiConfg();
    }


}
