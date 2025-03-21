package org.armada.galileo.plugin.util;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import lombok.extern.slf4j.Slf4j;
import org.armada.galileo.common.util.CommonUtil;
import org.armada.galileo.exception.BizException;

import java.util.Properties;
import java.util.concurrent.Executor;

/**
 * 操作 nacos 的工具类, 需要注意 引入的 nacos-client 版本号要与 nacos 服务端版本号一致
 *
 * @author wangxiaobo
 * @date 2021年5月23日
 */
@Slf4j
public class PluginNacosUtil {

    private String serverAddr;

    private String configFileId;

    private String configGroup;

    public PluginNacosUtil(String serverAddr, String configFileHeader) {

        if (CommonUtil.isEmpty(configFileHeader)) {
            throw new RuntimeException("serverAddr can not be null");
        }

        if (CommonUtil.isEmpty(configFileHeader)) {
            throw new RuntimeException("configFileHeader can not be null");
        }

        this.serverAddr = serverAddr;

        this.configFileId = configFileHeader + "_nova_plugin.json";

        this.configGroup = "nova_plugin";
    }

    public String readAllFormValue() {
        String configs = readFromNacos(configFileId, configGroup);
        return configs;
    }

    public void updateFormDefines(String configValue) {
        writeToNacos(configFileId, configGroup, configValue);
    }

    // 监听配置
    public void watchFormValueChange(NacosListener listener) throws Exception {
        Properties properties = new Properties();
        properties.put("serverAddr", serverAddr);
        ConfigService configService = NacosFactory.createConfigService(properties);

        configService.addListener(configFileId, configGroup, new Listener() {

            @Override
            public void receiveConfigInfo(String configInfo) {
                listener.afterChange(configInfo);
            }

            @Override
            public Executor getExecutor() {
                return null;
            }
        });
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

    public static interface NacosListener {
        public void afterChange(String nacosConfigs);
    }

}
