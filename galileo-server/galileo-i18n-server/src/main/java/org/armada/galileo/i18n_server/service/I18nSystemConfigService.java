package org.armada.galileo.i18n_server.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.armada.galileo.common.util.JsonUtil;
import org.armada.galileo.i18n_server.dal.entity.I18nSystemConfig;
import org.armada.galileo.i18n_server.dal.mapper.I18nSystemConfigMapper;
import org.armada.galileo.i18n_server.xunfei_api.XunFeiConfig;
import org.armada.galileo.i18n_server.xunfei_api.XunFeiUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * @author xiaobo
 * @date 2023/9/6 20:06
 */
@Service
public class I18nSystemConfigService {

    private String xunFeiKey = "XUN_FEI_KEY";

    @Autowired
    private I18nSystemConfigMapper i18nSystemConfigMapper;

    @PostConstruct
    public void init() {
        XunFeiConfig cfg = checkXunFeiConfg();
        if (cfg != null) {
            XunFeiUtil.updateConfig(cfg);
        }
    }

    public void updateXunFeiConfig(XunFeiConfig cfg) {

        I18nSystemConfig record = null;

        QueryWrapper<I18nSystemConfig> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("config_key", xunFeiKey);
        List<I18nSystemConfig> list = i18nSystemConfigMapper.selectList(queryWrapper);

        if (list.size() > 0) {
            record = list.get(0);
            record.setConfigValue(JsonUtil.toJson(cfg));
            i18nSystemConfigMapper.updateById(record);
        } else {
            record = new I18nSystemConfig();
            record.setConfigKey(xunFeiKey);
            record.setConfigValue(JsonUtil.toJson(cfg));
            i18nSystemConfigMapper.insert(record);
        }

        XunFeiUtil.updateConfig(cfg);
    }

    public XunFeiConfig checkXunFeiConfg() {
        QueryWrapper<I18nSystemConfig> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("config_key", xunFeiKey);
        List<I18nSystemConfig> list = i18nSystemConfigMapper.selectList(queryWrapper);
        if (list.size() > 0) {
            I18nSystemConfig record = list.get(0);

            XunFeiConfig cfg = JsonUtil.fromJson(record.getConfigValue(), XunFeiConfig.class);
            return cfg;
        }
        return null;
    }


}
