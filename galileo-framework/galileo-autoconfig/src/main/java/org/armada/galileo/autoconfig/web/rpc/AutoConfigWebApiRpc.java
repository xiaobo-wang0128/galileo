package org.armada.galileo.autoconfig.web.rpc;

import lombok.extern.slf4j.Slf4j;
import org.armada.galileo.autoconfig.domain.ConfigSubmitVo;
import org.armada.galileo.autoconfig.form.ATFormGroup;
import org.armada.galileo.autoconfig.util.AutoConfigEntity;
import org.armada.galileo.autoconfig.util.AutoConfigParser;
import org.armada.galileo.autoconfig.util.AutoConfigSpringAspect;
import org.armada.galileo.common.util.JsonUtil;
import org.armada.galileo.common.util.ValidateUtil;
import org.armada.galileo.exception.BizException;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import java.util.*;


@Slf4j
public class AutoConfigWebApiRpc {

    @Autowired
    private AutoConfigSpringAspect autoConfigSpringAspect;

    public Map<String, String> getConfigByKeys(String[] configKeys) {
        Map<String, String> map = new LinkedHashMap<>();
        for (String configKey : configKeys) {
            if (configKey.indexOf("->") == -1) {
                throw new BizException("configKeys 的格式为[configClass->ffield]");
            }
            String[] tmps = configKey.split("->");
            String v = autoConfigSpringAspect.getConfig(tmps[0], tmps[1]);
            map.put(configKey, v);
        }
        return map;
    }

    public List<ConfigSubmitVo> getAllConfigForms(String[] configClassList) throws Exception {
        List<ConfigSubmitVo> resut = new ArrayList<>();
        for (String clz : configClassList) {
            Class cls = null;
            try {
                cls = Class.forName(clz);
            } catch (Exception e) {
                throw new BizException("配置类不存在: " + clz);
            }

            ATFormGroup form = AutoConfigParser.parseForm(cls);
            AutoConfigEntity entity = autoConfigSpringAspect.getConfig(clz);
            String cfgValue = entity.getConfigValue();
            Object configValue = JsonUtil.fromJson(cfgValue, cls);

            ConfigSubmitVo vo = new ConfigSubmitVo();
            vo.setForm(form);
            vo.setValue(configValue);
            resut.add(vo);
        }
        return resut;
    }

    public void updateConfigValue(HttpServletRequest request) {

        Enumeration<String> names = request.getParameterNames();

        Map<String, String> mapValue = new HashMap<>();

        while (names.hasMoreElements()) {

            String key = names.nextElement();
            if (!key.startsWith("autoconfig___")) {
                continue;
            }

            String configId = key.substring("autoconfig___".length());

            String configValue = request.getParameter(key);

            log.info("configId: " + configId);
            log.info("configValue: " + configValue);


            Class cls = null;
            try {
                cls = Class.forName(configId);
            } catch (Exception e) {
                throw new BizException("class not exist: " + configId);
            }
            Object newObject = JsonUtil.fromJson(configValue, cls);
            ValidateUtil.validate(newObject);

            mapValue.put(configId, configValue);
        }

        for (Map.Entry<String, String> entry : mapValue.entrySet()) {
            autoConfigSpringAspect.updateConfig(entry.getKey(), entry.getValue());
        }

    }

    /**
     * 获取配置值
     *
     * @param clz
     * @return
     * @throws Exception
     */
    public Object getConfigValue(String clz) throws Exception {
        Class cls = null;
        try {
            cls = Class.forName(clz);
        } catch (Exception e) {
            throw new BizException("配置类不存在: " + clz);
        }
        AutoConfigEntity entity = autoConfigSpringAspect.getConfig(clz);
        String cfgValue = entity.getConfigValue();
        Object configValue = JsonUtil.fromJson(cfgValue, cls);
        return configValue;
    }


}
