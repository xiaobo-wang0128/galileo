package org.armada.galileo.autoconfig.web.rpc;

import java.util.*;

import javax.servlet.http.HttpServletRequest;

import org.armada.galileo.autoconfig.AutoConfigBean;
import org.armada.galileo.autoconfig.NacosConfig;
import org.armada.galileo.autoconfig.form.ATFormGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;

@Controller
public class AutoConfigWebApiRpc {


    @Autowired
    private ApplicationContext applicationContext;


    public Map<String, Object> getAllConfigForms() {

        AutoConfigBean autoConfigBean = applicationContext.getBean(AutoConfigBean.class);

        Map<String, Object> map = new HashMap<String, Object>();

        //List<ATFormGroup> forms = autoConfigBean.loadAllConfigForms();
        List<ATFormGroup> forms = autoConfigBean.getFormDefines();

        Collections.sort(forms, new Comparator<ATFormGroup>() {
            @Override
            public int compare(ATFormGroup o1, ATFormGroup o2) {
                return o1.getSort().compareTo(o2.getSort());
            }
        });

        map.put("form", forms);

        // map.put("value", autoConfigBean.loadAllConfigValues());
        map.put("value", autoConfigBean.getCurrentValues());

        return map;
    }

    public void updateConfigValue(HttpServletRequest request) {

        AutoConfigBean autoConfigBean = applicationContext.getBean(AutoConfigBean.class);

        Enumeration<String> names = request.getParameterNames();

        List<NacosConfig> nacosConfigs = new ArrayList<NacosConfig>();

        while (names.hasMoreElements()) {

            String key = names.nextElement();
            if (!key.startsWith("autoconfig_")) {
                continue;
            }

            String configId = key.substring("autoconfig_".length());

            String configValue = request.getParameter(key);

            nacosConfigs.add(new NacosConfig(configId, configValue));

        }

        autoConfigBean.updateFormValues(nacosConfigs);
    }

}
