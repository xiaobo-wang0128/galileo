package org.armada.galileo.autoconfig;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.armada.galileo.autoconfig.NacosUtil.NacosListener;
import org.armada.galileo.autoconfig.form.ATFormGroup;
import org.armada.galileo.autoconfig.util.ObjectDefaultValue;
import org.armada.galileo.common.util.CommonUtil;
import org.armada.galileo.common.util.JsonUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.util.ReflectionUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AutoConfigBean implements ApplicationContextAware {

    private NacosUtil nacosUtil;

    public AutoConfigBean(String serverAddr, String configFileHeader) {
        this.nacosUtil = new NacosUtil(serverAddr, configFileHeader);
    }

    private ApplicationContext applicationContext;

    /**
     * cache spring beanname: { className: beanName}
     */
    private static Map<String, String> springBeanName = new HashMap<>();

    public List<ATFormGroup> getFormDefines() {

        List<ATFormGroup> configGroups = new ArrayList<>();

        // 缓存 需要初始化到远程的表单数据
        Map<String, AutoConfigGalileo> configs = applicationContext.getBeansOfType(AutoConfigGalileo.class);

        for (Map.Entry<String, AutoConfigGalileo> entry : configs.entrySet()) {

            Class<?> cls = entry.getValue().getClass();

            // 这里是 spring 生成的 cblib 内部类， 需要拿真实的 class
            String clsName = cls.getName();

            if (clsName.indexOf("$$") != -1) {
                clsName = clsName.substring(0, clsName.indexOf("$$"));
                try {
                    cls = Class.forName(clsName);

                } catch (Exception e) {
                    log.error("[autoconfig] 类加载失败: " + e.getMessage(), e);
                    continue;
                }
            }

            ATFormGroup group = AutoConfigParser.parseForm(cls);
            configGroups.add(group);

        }
        return configGroups;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {

        this.applicationContext = applicationContext;

        // -------- step1 --------
        // 系统启动后，先读取本应用所有的可配置类，读取表单信息，并更新至 nacos

        Map<String, AutoConfigGalileo> configs = applicationContext.getBeansOfType(AutoConfigGalileo.class);

        List<NacosConfig> nacosConfig = new ArrayList<>();

        // 缓存 需要初始化到远程的表单数据
        Map<String, Class<?>> needInintToRemote = new HashMap<>();

        for (Map.Entry<String, AutoConfigGalileo> entry : configs.entrySet()) {

            Class<?> cls = entry.getValue().getClass();

            // 这里是 spring 生成的 cblib 内部类， 需要拿真实的 class
            String clsName = cls.getName();

            if (clsName.indexOf("$$") != -1) {
                clsName = clsName.substring(0, clsName.indexOf("$$"));
                try {
                    cls = Class.forName(clsName);
                } catch (Exception e) {
                    log.error("[autoconfig] 类加载失败: " + e.getMessage(), e);
                    continue;
                }
            }

            String beanName = null;
            if (cls.isAnnotationPresent(Configuration.class)) {
                beanName = cls.getAnnotation(Configuration.class).value();
            }
            //
            else if (cls.isAnnotationPresent(Component.class)) {
                beanName = cls.getAnnotation(Component.class).value();
            }
            //
            else if (cls.isAnnotationPresent(Service.class)) {
                beanName = cls.getAnnotation(Service.class).value();
            }
            //
            else if (cls.isAnnotationPresent(Controller.class)) {
                beanName = cls.getAnnotation(Controller.class).value();
            }

            if (CommonUtil.isNotEmpty(beanName)) {
                springBeanName.put(cls.getName(), beanName);
            }

            ATFormGroup group = AutoConfigParser.parseForm(cls);

            nacosConfig.add(new NacosConfig(group.getClassName(), JsonUtil.toJson(group)));

            needInintToRemote.put(clsName, cls);

        }
        // nacosUtil.updateFormDefines(nacosConfig, false);

        // -------- step2 --------
        // 读取 nacos 保存的配置，并更新至本地缓存的配置对象
        List<NacosConfig> nacosConfigs = nacosUtil.readAllFormValue();

        // 更新成功的配置项
        List<NacosConfig> updatedConfig = updateLocalConfigValue(nacosConfigs, applicationContext);

        // 新增、删除配置类时， 需同步更新远程 nacos 的配置项
        if (needInintToRemote.size() > 0) {

            List<NacosConfig> newConfigs = new ArrayList<NacosConfig>();

            main:
            for (Map.Entry<String, Class<?>> entry : needInintToRemote.entrySet()) {

                for (NacosConfig updated : updatedConfig) {
                    if (updated.getConfigId().equals(entry.getKey())) {
                        continue main;
                    }
                }

                try {
                    Object obj = entry.getValue().newInstance();

                    newConfigs.add(new NacosConfig(entry.getKey(), JsonUtil.toJson(obj)));

                } catch (Exception e) {
                    throw new RuntimeException("[autoconfig] 初始化失败:" + e.getMessage(), e);
                }
            }

            newConfigs.addAll(updatedConfig);

            if (!compare(nacosConfigs, newConfigs)) {
                updateFormValues(newConfigs);
            }

        }

        // -------- step3 --------
        // 监听配置信息变化
        try {
            nacosUtil.watchFormValueChange(new NacosListener() {
                public void receive(List<NacosConfig> remoteConfigs) {
                    updateLocalConfigValue(remoteConfigs, applicationContext);
                }
            });
        } catch (Exception e) {
            log.error("[autoconfig] nacos listen failed: " + e.getMessage(), e);
        }

    }

    /**
     * 更新本地配置
     *
     * @param nacosConfigs
     * @param applicationContext
     * @return 已更新成功的本地配置 key
     */
    private List<NacosConfig> updateLocalConfigValue(List<NacosConfig> nacosConfigs, ApplicationContext applicationContext) {

        List<NacosConfig> updateSuccess = new ArrayList<NacosConfig>();

        if (nacosConfigs == null || nacosConfigs.size() == 0) {
            return updateSuccess;
        }

        for (NacosConfig nc : nacosConfigs) {

            Class<?> cls = null;
            try {
                cls = Class.forName(nc.getConfigId());
            } catch (Exception e) {
                log.warn("非当前应用的配置项：" + nc.getConfigId());
                continue;
            }
            try {

                Object remoteConfig = JsonUtil.fromJson(nc.getConfigValue(), cls);

                if (remoteConfig == null) {
                    log.error("远程配置解析出错，将启用本地默认配置, config class:{} ", nc.getConfigId());
                }

                Object cacheConfig = null;

                String clsName = cls.getName();
                if (springBeanName.get(clsName) != null) {
                    cacheConfig = applicationContext.getBean(springBeanName.get(clsName));
                } else {
                    cacheConfig = applicationContext.getBean(cls);
                }
                if (remoteConfig != null) {
                    BeanUtils.copyProperties(remoteConfig, cacheConfig);
                }
                updateSuccess.add(nc);

                log.info("[autoconfig] 远程配置加载成功, configId: {}, configValue: {} ", nc.getConfigId(), nc.getConfigValue());

            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }

        return updateSuccess;

    }

    private boolean compare(List<NacosConfig> oldConfigs, List<NacosConfig> newConfigs) {

        if (oldConfigs == null || oldConfigs.isEmpty() || newConfigs == null || newConfigs.isEmpty()) {
            return false;
        }
        if (oldConfigs.size() != newConfigs.size()) {
            return false;
        }

        Comparator<NacosConfig> comparator = new Comparator<NacosConfig>() {
            @Override
            public int compare(NacosConfig o1, NacosConfig o2) {
                String v1 = o1.getConfigId() + o1.getConfigValue();
                String v2 = o2.getConfigId() + o2.getConfigValue();
                return v1.compareTo(v2);
            }
        };
        Collections.sort(oldConfigs, comparator);
        Collections.sort(newConfigs, comparator);

        String s1 = CommonUtil.join(oldConfigs.stream().map(e -> e.getConfigId() + e.getConfigValue()).collect(Collectors.toList()), ",");
        String s2 = CommonUtil.join(newConfigs.stream().map(e -> e.getConfigId() + e.getConfigValue()).collect(Collectors.toList()), ",");

        return s1.equals(s2);

    }

    public void updateFormValues(List<NacosConfig> nacosConfigs) {
        nacosUtil.updateFormValues(nacosConfigs);
    }


    public List<ATFormGroup> loadAllConfigForms() {
        List<ATFormGroup> result = new ArrayList<ATFormGroup>();
        for (NacosConfig config : nacosUtil.readAllFormConfig()) {
            try {
                ATFormGroup group = JsonUtil.fromJson(config.getConfigValue(), ATFormGroup.class);
                result.add(group);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                throw new RuntimeException("[autocnfig] 表单读取失败" + e.getMessage());
            }
        }
        return result;
    }

    public Map<String, Object> loadAllConfigValues() {

        Map<String, Object> result = new HashMap<String, Object>();

        for (NacosConfig config : nacosUtil.readAllFormValue()) {

            try {
                Class<?> cls = Class.forName(config.getConfigId());

                Object obj = JsonUtil.fromJson(config.getConfigValue(), cls);

                result.put(config.getConfigId(), obj);
            }
            // 类加载失败，说明 配置类已从代码中删除
            catch (ClassNotFoundException e) {
                log.warn("[autoconfig] 配置信息读取失败, " + e.getMessage());
            } catch (Exception e) {
                throw new RuntimeException("[autoconfig] 配置信息读取失败" + e.getMessage());
            }
        }
        return result;
    }

    public Map<String, Object> getCurrentValues() {

        Map<String, AutoConfigGalileo> configs = applicationContext.getBeansOfType(AutoConfigGalileo.class);

        Map<String, Object> values = new HashMap<String, Object>();

        for (Map.Entry<String, AutoConfigGalileo> entry : configs.entrySet()) {

            Class<?> cls = entry.getValue().getClass();

            // 这里是 spring 生成的 cblib 内部类， 需要拿真实的 class
            String clsName = cls.getName();

            if (clsName.indexOf("$$") != -1) {
                clsName = clsName.substring(0, clsName.indexOf("$$"));
                try {
                    cls = Class.forName(clsName);
                } catch (Exception e) {
                    log.error("[autoconfig] 类加载失败: " + e.getMessage(), e);
                    continue;
                }
            }
            Object obj = null;
            try {
                obj = cls.newInstance();
            } catch (Exception e) {
                throw new RuntimeException("[autoconfig] " + clsName + "需要一个无参的构造函数");
            }
            BeanUtils.copyProperties(entry.getValue(), obj);

            // ReflectionUtils.get

            // 解决配置项没有默认值的问题
            for (Field f : cls.getFields()) {

                ReflectionUtils.makeAccessible(f);

                Object tmpValue = ReflectionUtils.getField(f, obj);

                if (tmpValue == null) {
                    tmpValue = new ObjectDefaultValue(f.getType()).generateMock();
                    ReflectionUtils.makeAccessible(f);
                    ReflectionUtils.setField(f, obj, tmpValue);
                }
            }

            values.put(clsName, obj);
        }

        return values;

    }

}
