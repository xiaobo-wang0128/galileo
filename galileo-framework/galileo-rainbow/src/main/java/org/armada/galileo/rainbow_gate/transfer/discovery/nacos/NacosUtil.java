package org.armada.galileo.rainbow_gate.transfer.discovery.nacos;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import lombok.extern.slf4j.Slf4j;
import org.armada.galileo.common.util.AssertUtil;
import org.armada.galileo.common.util.CommonUtil;
import org.armada.galileo.exception.BizException;

import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * 操作 nacos 的工具类, 需要注意 引入的 nacos-client 版本号要与 nacos 服务端版本号一致
 *
 * @author wangxiaobo
 * @date 2021年5月23日
 */
@Slf4j
public class NacosUtil {

    private String serverAddr;

    private String dataId;

    private String group;

    public NacosUtil(String serverAddr, String dataId, String group) {
        AssertUtil.isNotNull(serverAddr);
        AssertUtil.isNotNull(dataId);
        AssertUtil.isNotNull(group);

        this.serverAddr = serverAddr;
        this.dataId = dataId;
        this.group = group;
    }

    public static interface NacosListener {
        public void receive(String nacosValue);
    }

    private Executor ex = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    // 监听配置
    public void watch(NacosListener listener) {
        try {
            Properties properties = new Properties();
            properties.put("serverAddr", serverAddr);
            ConfigService configService = NacosFactory.createConfigService(properties);

            configService.addListener(dataId, group, new Listener() {
                @Override
                public void receiveConfigInfo(String configInfo) {
                    log.info("[nacos] 监听到数据发生变更, dataId:{}, group:{}, value:{}", dataId, group, configInfo);
                    listener.receive(configInfo);
                }

                @Override
                public Executor getExecutor() {
                    return ex;
                }
            });
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }


    public String readValue() {

        String content = null;
        ConfigService configService = null;
        try {

            Properties properties = new Properties();
            properties.put("serverAddr", serverAddr);

            configService = NacosFactory.createConfigService(properties);

            configService = NacosFactory.createConfigService(serverAddr);

            content = configService.getConfig(dataId, group, 5000);

        } catch (Exception e) {
            log.error("nacos配置获取失败" + e.getMessage(), e);
        } finally {
            try {
                configService.shutDown();
            } catch (Exception e) {
            }
        }
        return content;
    }

    public void writeValue(String configValue) {
        ConfigService configService = null;
        try {
            // 初始化配置服务，控制台通过示例代码自动获取下面参数

            Properties properties = new Properties();
            properties.put("serverAddr", serverAddr);
            configService = NacosFactory.createConfigService(properties);

            boolean isPublishOk = configService.publishConfig(dataId, group, configValue);
            if (!isPublishOk) {
                throw new BizException("发布失败");
            }

        } catch (NacosException e) {
            log.error("nacos配置发布失败" + e.getMessage(), e);
            throw new BizException("nacos配置发布失败:" + e.getMessage());
        } finally {
            try {
                configService.shutDown();
            } catch (Exception e) {
            }
        }
    }

}
