package org.armada.galileo.plugin.util;

import com.google.gson.reflect.TypeToken;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.armada.galileo.common.loader.FileLoader;
import org.armada.galileo.common.util.CommonUtil;
import org.armada.galileo.common.util.JsonUtil;
import org.armada.galileo.plugin.annotation.NovaPlugin;
import org.armada.galileo.plugin.annotation.NovaPluginImpl;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.AbstractApplicationContext;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author xiaobo
 * @date 2021/10/27 3:46 下午
 */
@Slf4j
public class NovaPluginBean implements ApplicationContextAware {

    private static final String pluginExport = "nova_flow/plugin-export.conf";

    private static final String pluginDisabled = "_disabled_";

    /**
     * 当前生效的插件
     */
    public static final Map<String, String> pluginActive = new ConcurrentHashMap<>();

    /**
     * 所有插件集合
     */
    private List<PluginGroup> allGroups;

    /**
     * nacos
     */
    private PluginNacosUtil pluginNacosUtil;

    private static Object lock = new Object();

    private static AbstractApplicationContext applicationContext;

    /**
     * 获取当前生效的插件实现类
     *
     * @param cls
     * @param <T>
     * @return bean
     */
    public static <T> T getActiveClassBean(Class<T> cls) {

        String clsName = cls.getName();
        String cacheImpl = pluginActive.get(clsName);
        if (cacheImpl == null) {
            if (cls.isAnnotationPresent(NovaPlugin.class)) {
                log.warn("[nova-plugin] 未找到插件实现类: " + clsName);
                return null;
            }
            throw new RuntimeException("[nova-plugin] 插件定义不存在: " + clsName + ", 请确认该 interface 是否添加 @NovaPlugin 注解");
        }
        if (pluginDisabled.equals(cacheImpl)) {
            log.warn("[nova-plugin] 插件被禁用: " + clsName);
            return null;
        }

        Class<?> implClass = null;
        Object bean = null;

        try {

            implClass = Class.forName(cacheImpl);
            bean = applicationContext.getBean(implClass);

        } catch (ClassNotFoundException e) {

            throw new RuntimeException("[nova-plugin] 没有找到实现类: " + cacheImpl);

        } catch (NoSuchBeanDefinitionException e) {

            synchronized (lock) {

                bean = applicationContext.getAutowireCapableBeanFactory().createBean(implClass);

                // AbstractAutowireCapableBeanFactory

                ConfigurableListableBeanFactory beanFactory = applicationContext.getBeanFactory();

                beanFactory.registerSingleton(implClass.getName(), bean);

            }

        }
        return (T) bean;
    }


    public NovaPluginBean(String serverAddr, String configFileHeader) {
        this.pluginNacosUtil = new PluginNacosUtil(serverAddr, configFileHeader);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {

        // applicationContext.getBeanF
        NovaPluginBean.applicationContext = (AbstractApplicationContext) applicationContext;

        List<byte[]> fileBytes = FileLoader.loadResourceFiles(pluginExport);

        if (fileBytes == null || fileBytes.size() == 0) {
            return;
        }

        Map<String, PluginInterface> pluginInterfaceCache = new HashMap<>();

        Map<String, Boolean> hasExist = new HashMap<>();

        for (byte[] file : fileBytes) {

            String fileContent = null;
            try {
                fileContent = new String(file, "utf-8");
            } catch (Exception e) {
                log.error(pluginExport + "文件读取异常");
                log.error(e.getMessage(), e);
            }

            String[] lines = fileContent.split("\n");

            if (lines == null || lines.length == 0) {
                continue;
            }

            for (String line : lines) {

                if (CommonUtil.isEmpty(line)) {
                    continue;
                }
                line = line.trim();

                if (line.startsWith("#")) {
                    continue;
                }

                if (hasExist.get(line) != null && hasExist.get(line)) {
                    log.warn("[nova-plugin] 重复定义: " + line);
                    continue;
                }
                hasExist.put(line, true);

                Class<?> pluginImplClass = null;
                try {
                    pluginImplClass = Class.forName(line);
                } catch (Exception e) {
                    throw new RuntimeException("实现类不存在: " + line);
                }

                if (!pluginImplClass.isAnnotationPresent(NovaPluginImpl.class)) {
                    throw new RuntimeException(pluginImplClass.getName() + "必须添加注解 @NovaPluginImpl ");
                }

                Class<?>[] interfaces = pluginImplClass.getInterfaces();
                if (interfaces == null || interfaces.length == 0) {
                    throw new RuntimeException(pluginImplClass.getName() + "必须实现一个插件接口");
                }

                List<Class<?>> interfacePlugin = Arrays.stream(pluginImplClass.getInterfaces()).filter(e -> e.isAnnotationPresent(NovaPlugin.class)).collect(Collectors.toList());

                if (interfacePlugin == null || interfacePlugin.isEmpty()) {
                    throw new RuntimeException(pluginImplClass.getName() + "的插件接口'" + interfaces[0].getName() + "'必须添加注解 @NovaPlugin");
                }

                Class<?> pluginInterfaceCls = interfacePlugin.get(0);

                PluginInterface pluginInterfaceObj = pluginInterfaceCache.get(pluginInterfaceCls.getName());
                if (pluginInterfaceObj == null) {

                    NovaPlugin np = pluginInterfaceCls.getAnnotation(NovaPlugin.class);

                    String groupName = np.group();
                    String pluginName = np.name();
                    String pluginInterface = pluginInterfaceCls.getName();
                    List<PluginImpl> pluginImpls = new ArrayList<PluginImpl>();

                    pluginInterfaceObj = new PluginInterface(groupName, pluginName, pluginInterface, pluginImpls, np.sort());

                    pluginInterfaceCache.put(pluginInterfaceCls.getName(), pluginInterfaceObj);
                }

                List<PluginImpl> pluginImpls = pluginInterfaceObj.getPluginImpls();

                NovaPluginImpl novaPluginImpl = pluginImplClass.getAnnotation(NovaPluginImpl.class);
                String implName = novaPluginImpl.name();
                String implDesc = novaPluginImpl.desc();
                String implClass = pluginImplClass.getName();

                PluginImpl pluginImpl = new PluginImpl(implName, implClass, implDesc);
                pluginImpls.add(pluginImpl);
            }
        }

        List<PluginInterface> allList = pluginInterfaceCache.entrySet().stream().map(e -> e.getValue()).collect(Collectors.toList());

        Map<String, PluginGroup> groupCache = new LinkedHashMap();

        for (PluginInterface obj : allList) {
            String groupName = obj.getGroupName();
            PluginGroup group = groupCache.get(groupName);

            if (group == null) {
                group = new PluginGroup(groupName, new ArrayList<PluginInterface>());
                groupCache.put(groupName, group);
            }
            group.getInterfaceOptions().add(obj);

            //默认标识当前实现是禁用
            pluginActive.put(obj.getPluginInterface(), pluginDisabled);
        }

        List<PluginGroup> groups = groupCache.entrySet().stream().map(e -> e.getValue()).collect(Collectors.toList());

        // 排序
        for (PluginGroup group : groups) {

            group.getInterfaceOptions().forEach(e -> {
                e.getPluginImpls().add(0, new PluginImpl("禁用", pluginDisabled, null));
            });

            Collections.sort(group.getInterfaceOptions(), new Comparator<PluginInterface>() {
                @Override
                public int compare(PluginInterface o1, PluginInterface o2) {
                    return o1.getSort().compareTo(o2.getSort());
                }
            });
        }
        Collections.sort(groups, new Comparator<PluginGroup>() {
            @Override
            public int compare(PluginGroup o1, PluginGroup o2) {
                return o1.getInterfaceOptions().get(0).getSort().compareTo(o2.getInterfaceOptions().get(0).getSort());
            }
        });


        this.allGroups = groups;

        // 获取远程配置，用于覆盖本地配置
        String remoteConfig = pluginNacosUtil.readAllFormValue();
        List<RemotePluginConfig> remoteConfigs = JsonUtil.fromJson(remoteConfig, new TypeToken<List<RemotePluginConfig>>() {
        }.getType());
        updateLocalConfig(remoteConfigs);
    }


    /**
     * 更新插件开关、实现类
     *
     * @param configs
     */
    public void updateLocalConfig(List<RemotePluginConfig> configs) {
        if (configs == null || configs.isEmpty()) {
            return;
        }

        boolean hasChange = false;

        for (RemotePluginConfig config : configs) {

            String impl = CommonUtil.isNotEmpty(config.getPluginImpl()) ? config.getPluginImpl() : pluginDisabled;

            if (impl.equals(pluginActive.get(config.getPluginClass()))) {
                continue;
            }

            // 如果开始了配置，需要在此校验该类是否在当前容器中
            if (!pluginDisabled.equals(impl)) {
                try {
                    Class.forName(impl);
                } catch (Exception e) {
                    log.error("[nova-config] 配置切换失败, 没有找到实现类: " + impl);
                    continue;
                }
            }

            pluginActive.put(config.getPluginClass(), impl);

            hasChange = true;
        }
        if (hasChange) {
            pluginNacosUtil.updateFormDefines(JsonUtil.toJsonPretty(configs));
        }
    }

    public Map<String, RemotePluginConfig> getCurrentConfig() {

        Map<String, RemotePluginConfig> result = new HashMap<>();

        for (Map.Entry<String, String> entry : pluginActive.entrySet()) {
            RemotePluginConfig cfg = new RemotePluginConfig(entry.getKey(), entry.getValue());
            result.put(entry.getKey(), cfg);
        }

        return result;
    }

    /**
     * 获取所有可选插件
     *
     * @return
     */
    public List<PluginGroup> getAllGroups() {
        return allGroups;
    }


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RemotePluginConfig {
        private String pluginClass;
        private String pluginImpl;
    }


    /**
     * 插件可选项
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PluginGroup {
        private String groupName;
        private List<PluginInterface> interfaceOptions;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PluginInterface {
        private String groupName;
        private String pluginName;
        private String pluginInterface;
        private List<PluginImpl> pluginImpls;
        private Integer sort;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PluginImpl {
        private String implName;
        private String implClass;
        private String implDesc;
    }
}
