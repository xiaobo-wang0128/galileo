package org.armada.galileo.autoconfig.util;

import com.baomidou.mybatisplus.core.override.MybatisMapperProxy;
import lombok.extern.slf4j.Slf4j;
import org.armada.galileo.autoconfig.annotation.AutoConfig;
import org.armada.galileo.common.redis.RedisUtil;
import org.armada.galileo.common.util.AssertUtil;
import org.armada.galileo.common.util.CommonUtil;
import org.armada.galileo.common.util.JsonUtil;
import org.armada.galileo.common.util.ValidateUtil;
import org.armada.galileo.exception.BizException;
import org.armada.galileo.model.domain.LoginUser;
import org.armada.galileo.model.domain.ThreadUser;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 全局定时任务锁
 *
 * @author xiaobo
 * @date 2022/12/18 17:25
 */
@Aspect
@Slf4j
public class AutoConfigSpringAspect {

    private AutoConfigEntityMapper mapper;

    private RedisUtil redisUtil;

    private String tableName;

    public AutoConfigSpringAspect(AutoConfigEntityMapper mapper, RedisUtil redisUtil) {

        // MybatisMapperProxy proxy = (MybatisMapperProxy)mapper;

        Field f = ReflectionUtils.findField(Proxy.class, "h");
        ReflectionUtils.makeAccessible(f);
        InvocationHandler v = (InvocationHandler) ReflectionUtils.getField(f, mapper);

        MybatisMapperProxy mybatisMapperProxy = (MybatisMapperProxy) v;

        Field fs = ReflectionUtils.findField(MybatisMapperProxy.class, "mapperInterface");
        ReflectionUtils.makeAccessible(fs);
        Class mapperInterface = (Class) ReflectionUtils.getField(fs, mybatisMapperProxy);

        String cls = mapperInterface.getName();
        cls = cls.substring(cls.lastIndexOf(".") + 1);
        if (cls.endsWith("Mapper")) {
            cls = cls.substring(0, cls.length() - 6);
        }
        String tableName = CommonUtil.convertJavaField2DB(cls);
        this.tableName = tableName;
        this.mapper = mapper;
        this.redisUtil = redisUtil;
    }

    private AutoConfigEntity getConfigBase(String configClass) {

        LoginUser loginUser = ThreadUser.get();
        if (loginUser == null) {
            throw new BizException("login user is null");
        }

        Class cls = null;
        try {
            cls = Class.forName(configClass);
        } catch (Exception e) {
            throw new BizException("config class not exist: " + configClass);
        }
        AutoConfig autoConfig = (AutoConfig) cls.getAnnotation(AutoConfig.class);
        if (autoConfig == null) {
            throw new BizException("config class need @AutoConfig, configClass: " + configClass);
        }

        TenantTypeEnum tenantType = autoConfig.type();

        Long tenantId = null;

        // 租户
        if (tenantType == TenantTypeEnum.TENANT) {
            tenantId = loginUser.getTenantId();
        }
        // 子机构
        else if (tenantType == TenantTypeEnum.COMPANY) {
            tenantId = loginUser.getCompanyId();
        }
        // 子机构
        else if (tenantType == TenantTypeEnum.WAREHOUSE) {
            tenantId = loginUser.getWarehouseId();
        }
        // 客户
        else if (tenantType == TenantTypeEnum.CUSTOMER) {
            tenantId = loginUser.getUserId();
        }
        // 登陆账号
        else if (tenantType == TenantTypeEnum.USER) {
            tenantId = loginUser.getUserId();
        }
        if (tenantId == null) {
            throw new BizException("无法获取到租户ID, 租户类型：{}, current user:{}", tenantType.toString(), JsonUtil.toJson(loginUser));
        }
        AutoConfigEntity entity = new AutoConfigEntity();
        entity.setTenantId(tenantId);
        entity.setTenantType(tenantType);
        entity.setConfigClass(configClass);
        return entity;
    }

    public AutoConfigEntity getConfig(String configClass) {

        AutoConfigEntity configBean = getConfigBase(configClass);

        AutoConfigEntity exist = mapper.selectConfig(tableName, configBean.getTenantId(), configBean.getTenantType(), configClass);

        if (exist != null) {
            return exist;
        } else {

            try {
                Class clz = Class.forName(configClass);
                Object obj = clz.newInstance();
                String configValue = JsonUtil.toJson(obj);
                configBean.setConfigValue(configValue);

                return configBean;

            } catch (Exception e) {
                log.error(e.getMessage(), e);
                throw new RuntimeException(e);
            }
        }

//        String redisKey = new StringBuilder().append(configBean.getTenantId()).append(configBean.getTenantType()).append(configBean.getConfigClass()).toString();
//        String json = redisUtil.get(CommonCacheKey.AUTO_CONFIG, redisKey);
//        if (json != null) {
//            configBean.setConfigValue(json);
//            return configBean;
//        }
//
//        synchronized (log) {
//            json = redisUtil.get(CommonCacheKey.AUTO_CONFIG, redisKey);
//            if (json != null) {
//                configBean.setConfigValue(json);
//                return configBean;
//            }
//
//            configBean = mapper.selectConfig(configBean.getTenantId(), configBean.getTenantType(), configClass);
//            redisUtil.set(CommonCacheKey.AUTO_CONFIG, redisKey, configBean.getConfigValue());
//        }
//        return configBean;
    }

    public void updateConfig(String configClass, String configValue) {

        AssertUtil.isNotNull(configValue);

        Class cls = null;
        try {
            cls = Class.forName(configClass);
        } catch (Exception e) {
            throw new BizException("class not exist: " + configClass);
        }
        Object newObject = JsonUtil.fromJson(configValue, cls);
        ValidateUtil.validate(newObject);

        AutoConfigEntity base = getConfigBase(configClass);

        AutoConfigEntity exist = mapper.selectConfig(tableName, base.getTenantId(), base.getTenantType(), configClass);

        Map<Field, Object> updateMap = new HashMap<>();

        if (exist == null) {

            mapper.insertConfig(tableName, base.getTenantId(), base.getTenantType(), base.getConfigClass(), configValue, System.currentTimeMillis());

            try {
                for (Field field : cls.getDeclaredFields()) {
                    ReflectionUtils.makeAccessible(field);
                    Object newV = ReflectionUtils.getField(field, newObject);
                    if (newV == null) {
                        continue;
                    }
                    if (newV != null) {
                        updateMap.put(field, newV);
                    }
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }

            exist = new AutoConfigEntity();
            exist.setConfigValue(configValue);
            exist.setConfigClass(base.getConfigClass());
            exist.setTenantType(base.getTenantType());
            exist.setTenantId(base.getTenantId());

        } else {
            mapper.updateConfig(tableName, exist.getTenantId(), exist.getTenantType(), exist.getConfigClass(), configValue, System.currentTimeMillis());

            // 对比 field value
            try {
                Object oldObject = JsonUtil.fromJson(exist.getConfigValue(), cls);

                for (Field field : cls.getDeclaredFields()) {
                    ReflectionUtils.makeAccessible(field);
                    Object oldV = ReflectionUtils.getField(field, oldObject);
                    Object newV = ReflectionUtils.getField(field, newObject);
                    if (newV == null) {
                        continue;
                    }
                    if (!Objects.equals(oldV, newV)) {
                        updateMap.put(field, newV);
                    }
                }

            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }

            exist.setConfigValue(configValue);
        }

        for (Map.Entry<Field, Object> entry : updateMap.entrySet()) {
            Field f = entry.getKey();
            Object v = entry.getValue();

            if (v == null) {
                continue;
            }

            String cacheV = null;
            if (f.getType().getName().startsWith("java.lang")) {
                cacheV = v.toString();
            } else if (f.isEnumConstant()) {
                cacheV = v.toString();
            } else {
                cacheV = JsonUtil.toJson(v);
            }

            String redisKey = new StringBuilder().append(exist.getTenantId()).append(exist.getTenantType()).append(exist.getConfigClass()).append(f.getName()).toString();

            redisUtil.set(CommonCacheKey.AUTO_CONFIG, redisKey, cacheV);
        }

    }


    public String getConfig(String configClass, String field) {

        AutoConfigEntity configBean = getConfigBase(configClass);

        String redisKey = new StringBuilder().append(configBean.getTenantId()).append(configBean.getTenantType()).append(configBean.getConfigClass()).append(field).toString();

        String cacheV = redisUtil.get(CommonCacheKey.AUTO_CONFIG, redisKey);

        log.info("get from redis, key: {}, v:{} ", redisKey, cacheV);

        if (cacheV != null) {
            return cacheV;
        }

        synchronized (log) {

            cacheV = redisUtil.get(CommonCacheKey.AUTO_CONFIG, redisKey);

            if (cacheV != null) {
                return cacheV;
            }

            configBean = getConfig(configClass);

            try {

                Class cls = Class.forName(configClass);

                Object newObject = JsonUtil.fromJson(configBean.getConfigValue(), cls);

                Field f = ReflectionUtils.findField(cls, field);

                if (f == null) {
                    throw new RuntimeException("f not exist: " + field);
                }

                ReflectionUtils.makeAccessible(f);

                Object v = ReflectionUtils.getField(f, newObject);

                cacheV = null;

                if (v != null) {
                    if (f.getType().getName().startsWith("java.lang")) {
                        cacheV = v.toString();
                    } else if (f.isEnumConstant()) {
                        cacheV = v.toString();
                    } else {
                        cacheV = JsonUtil.toJson(v);
                    }
                    redisUtil.set(CommonCacheKey.AUTO_CONFIG, redisKey, cacheV);
                }

            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }

        return cacheV;
    }


    @Pointcut("@within(org.armada.galileo.autoconfig.annotation.AutoConfig)")
    public void proxyAspect() {
    }


    @Around("proxyAspect()")
    public Object doInvoke(ProceedingJoinPoint joinPoint) throws Throwable {

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();

        Method method = signature.getMethod();

        String methodName = method.getName();

        if (methodName.startsWith("get")) {

            methodName = methodName.substring(3);

            String fieldName = methodName.substring(0, 1).toLowerCase() + methodName.substring(1);

            String configValue = getConfig(method.getDeclaringClass().getName(), fieldName);

            if (configValue == null) {
                return null;
            }

            String returnType = method.getReturnType().getName();

            Object returnV = null;

            if ("java.lang.Double".equals(returnType)) {
                returnV = Double.valueOf(configValue);
            } else if ("java.lang.Boolean".equals(returnType)) {
                returnV = Boolean.valueOf(configValue);
            } else if ("java.lang.Byte".equals(returnType)) {
                returnV = Byte.valueOf(configValue);
            } else if ("java.lang.Character".equals(returnType)) {
                returnV = configValue.toCharArray()[0];
            } else if ("java.lang.Integer".equals(returnType)) {
                returnV = Integer.valueOf(configValue);
            } else if ("java.lang.Long".equals(returnType)) {
                returnV = Long.valueOf(configValue);
            } else if ("java.lang.String".equals(returnType)) {
                returnV = configValue;
            } else if ("java.lang.Short".equals(returnType)) {
                returnV = Short.valueOf(configValue);
            } else {
                returnV = JsonUtil.fromJson(configValue, method.getReturnType());
            }

            log.info("[auto-config] class:{}, field:{}, value:{}", method.getDeclaringClass().getName(), fieldName, returnV);

            return returnV;
        }

        return joinPoint.proceed();
    }

}
