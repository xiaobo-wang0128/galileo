package org.armada.galileo.autoconfig;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.armada.galileo.autoconfig.annotation.ConfigGroup;
import org.armada.galileo.common.util.CommonUtil;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;

import lombok.extern.slf4j.Slf4j;
import org.armada.galileo.exception.BizException;

/**
 * 操作 nacos 的工具类, 需要注意 引入的 nacos-client 版本号要与 nacos 服务端版本号一致
 *
 * @author wangxiaobo
 * @date 2021年5月23日
 */
@Slf4j
public class NacosUtil {

    private String serverAddr;

    /**
     * 表单定义, 格式：
     *
     * <pre>
     * 配置类class = 表单信息json
     * </pre>
     */
    private String formDefineDataId;

    /**
     * 配置项的值：格式：
     *
     * <pre>
     * 配置类class = 配置项json
     * </pre>
     */
    private String formValueDataId;

    /**
     * 配置文件所在的分组
     */
    private String group;


    public NacosUtil(String serverAddr, String configFileHeader) {

        if (CommonUtil.isEmpty(configFileHeader)) {
            throw new RuntimeException("serverAddr can not be null");
        }

        if (CommonUtil.isEmpty(configFileHeader)) {
            throw new RuntimeException("configFileHeader can not be null");
        }

        this.serverAddr = serverAddr;

        this.formDefineDataId = configFileHeader + "_autoconfig_forms.properties";
        this.formValueDataId = configFileHeader + "_autoconfig_values.properties";
        this.group = "nova_autoconfig";
    }

    public List<NacosConfig> readAllFormConfig() {

        List<NacosConfig> forms = new ArrayList<NacosConfig>();

        String old = readFromNacos(formDefineDataId, group);

        if (CommonUtil.isNotEmpty(old)) {
            String[] tmps = old.split("\n");

            // 读取远程表单配置
            for (String config : tmps) {
                if (CommonUtil.isEmpty(config)) {
                    continue;
                }
                int index = config.indexOf("=");
                if (index == -1) {
                    continue;
                }
                String formId = config.substring(0, index).trim();
                String formValue = config.substring(index + 1).trim();

                forms.add(new NacosConfig(formId, formValue));
            }
        }
        return forms;
    }

    public List<NacosConfig> readAllFormValue() {

        String configs = readFromNacos(formValueDataId, group);

        if (CommonUtil.isEmpty(configs)) {
            return new ArrayList<NacosConfig>(0);
        }

        String[] tmps = configs.split("\n");

        List<NacosConfig> nacosConfigs = new ArrayList<>();

        for (String config : tmps) {
            int index = config.indexOf("=");
            if (index == -1) {
                continue;
            }
            String formId = config.substring(0, index).trim();
            String formValue = config.substring(index + 1).trim();

            if (CommonUtil.isEmpty(formId) || CommonUtil.isEmpty(formValue)) {
                continue;
            }

            nacosConfigs.add(new NacosConfig(formId, formValue));
        }

        return nacosConfigs;
    }

    public void updateFormDefines(List<NacosConfig> forms, boolean append) {

        Map<String, String> cachedForm = new LinkedHashMap<String, String>();

        if (append) {

            String old = readFromNacos(formDefineDataId, group);

            if (CommonUtil.isNotEmpty(old)) {
                String[] tmps = old.split("\n");

                // 读取远程表单配置
                for (String config : tmps) {
                    if (CommonUtil.isEmpty(config)) {
                        continue;
                    }
                    int index = config.indexOf("=");
                    if (index == -1) {
                        continue;
                    }
                    String formId = config.substring(0, index).trim();
                    String formValue = config.substring(index + 1).trim();

                    cachedForm.put(formId, formValue);
                }
            }
        }

        // 覆盖远程表单配置
        for (NacosConfig form : forms) {
            cachedForm.put(form.getConfigId(), form.getConfigValue());
        }

        if (cachedForm.size() == 0) {
            return;
        }

        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : cachedForm.entrySet()) {
            sb.append(entry.getKey()).append("=").append(entry.getValue()).append("\n\n");
        }

        writeToNacos(formDefineDataId, group, sb.toString());
    }

    private Executor ex = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    /**
     * 将表单配置值 写回至 nacos
     *
     * @param formValues
     */
    public void updateFormValues(List<NacosConfig> formValues) {

        // 前置校验
        AutoConfigParser.preCheck(formValues);

        StringBuilder sb = new StringBuilder();

        // 覆盖远程表单配置
        for (NacosConfig form : formValues) {
            sb.append(form.getConfigId()).append("=").append(form.getConfigValue()).append("\n\n");
        }
        writeToNacos(formValueDataId, group, sb.toString());

        // 后置通知
        ex.execute(()->{
            AutoConfigParser.afterModify(formValues);
        });

    }

    // 监听配置
    public void watchFormValueChange(NacosListener listener) throws Exception {
        Properties properties = new Properties();
        properties.put("serverAddr", serverAddr);
        ConfigService configService = NacosFactory.createConfigService(properties);

        configService.addListener(formValueDataId, group, new Listener() {

            @Override
            public void receiveConfigInfo(String configInfo) {

                List<NacosConfig> nacosConfigs = new ArrayList<>();

                String[] tmps = configInfo.split("\n");

                // 读取远程表单配置
                for (String config : tmps) {
                    if (CommonUtil.isEmpty(config)) {
                        continue;
                    }
                    int index = config.indexOf("=");
                    if (index == -1) {
                        continue;
                    }
                    String formId = config.substring(0, index).trim();
                    String formValue = config.substring(index + 1).trim();

                    nacosConfigs.add(new NacosConfig(formId, formValue));
                }

                listener.receive(nacosConfigs);
            }

            @Override
            public Executor getExecutor() {
                return null;
            }
        });
    }

    public static interface NacosListener {
        public void receive(List<NacosConfig> nacosConfigs);
    }

    private String readFromNacos(String dataId, String group) {

        String content = null;

        try {

            Properties properties = new Properties();
            properties.put("serverAddr", serverAddr);

            ConfigService configService = NacosFactory.createConfigService(properties);

            configService = NacosFactory.createConfigService(serverAddr);

            content = configService.getConfig(dataId, group, 5000);

        } catch (Exception e) {
            log.error("nacos配置获取失败" + e.getMessage(), e);
        }
        return content;
    }

    private void writeToNacos(String dataId, String group, String configValue) {
        try {
            // 初始化配置服务，控制台通过示例代码自动获取下面参数

            Properties properties = new Properties();
            properties.put("serverAddr", serverAddr);
            ConfigService configService = NacosFactory.createConfigService(properties);

            boolean isPublishOk = configService.publishConfig(dataId, group, configValue);
            if (!isPublishOk) {
                throw new BizException("发布失败");
            }
        } catch (NacosException e) {
            log.error("nacos配置发布失败" + e.getMessage(), e);
            throw new BizException("nacos配置发布失败:" + e.getMessage());
        }
    }

}
