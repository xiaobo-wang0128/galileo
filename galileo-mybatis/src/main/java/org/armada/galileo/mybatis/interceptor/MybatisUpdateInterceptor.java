//package org.armada.galileo.mybatis.adaptor;
//
//import com.esotericsoftware.reflectasm.MethodAccess;
//import org.armada.galileo.mybatis.BaseMysqlEntity;
//import com.hairoutech.wms.common.util.ContextHolder;
//import com.hairoutech.wms.core.domain.mysql.sys.dal.dto.SysUserDTO;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.ibatis.binding.MapperMethod;
//import org.apache.ibatis.executor.Executor;
//import org.apache.ibatis.mapping.MappedStatement;
//import org.apache.ibatis.plugin.*;
//
//import java.util.Date;
//import java.util.Map;
//import java.util.Properties;
//import java.util.concurrent.ConcurrentHashMap;
//
//
//@Slf4j
//@Intercepts({@Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class})})
//public class MybatisUpdateInterceptor implements Interceptor {
//
//    public Object intercept(Invocation invocation) throws Throwable {
//        Object param = invocation.getArgs()[1];
//        if (param != null) {
//            String currentUser = null;
//
//            SysUserDTO user = ContextHolder.getLoginUser();
//
//            if (user != null) {
//                currentUser = user.getUserNickname();
//            } else {
//                currentUser = "system";
//            }
//
//            // 如果继承了基类
//            if (param instanceof BaseMysqlEntity) {
//
//                BaseMysqlEntity entity = (BaseMysqlEntity) param;
//
//                // 数据库更新
//                if (entity.getId() != null) {
//
//                    entity.setUpdateUser(currentUser);
//
//                    entity.setUpdateTime(new Date());
//
//                }
//                // 数据插入
//                else {
//                    entity.setCreateUser(currentUser);
//                    entity.setUpdateUser(currentUser);
//                    // entity.setIsDeleted("n");
//
//                    entity.setCreateTime(new Date());
//                    entity.setUpdateTime(new Date());
//
//                }
//            }
//            // 没有继承基类，但类中定义了  createTime 等字段
//            else {
//
//                Object target = param;
//
//                //当前操作是否为批量更新
//                boolean isBatchUpdate = false;
//
//                // 兼容 updateById(entity)  update(entity, wrapper)
//                if (param instanceof MapperMethod.ParamMap) {
//                    MapperMethod.ParamMap paramMap = ((MapperMethod.ParamMap) param);
//
//                    if (paramMap.containsKey("et")) {
//                        target = paramMap.get("et");
//                    }
//                    if (paramMap.containsKey("ew")) {
//                        if (paramMap.get("ew") != null) {
//                            isBatchUpdate = true;
//                        }
//                    }
//                }
//
//                //只处理主键名 为id的表
//                if (hasField(target, "id")) {
//
//                    MethodAccess access = MethodAccess.get(target.getClass());
//
//                    Object id = access.invoke(target, "getId");
//
//                    Date now = new Date();
//                    // 更新 操作
//                    if (id != null) {
//                        if (hasField(target, "updateTime")) {
//                            access.invoke(target, "setUpdateTime", now);
//                        }
//                        if (hasField(target, "updateUser")) {
//                            access.invoke(target, "setUpdateUser", currentUser);
//                        }
//                    }
//                    //新增操作
//                    else {
//                        if (!isBatchUpdate) {
//                            if (hasField(target, "createTime")) {
//                                access.invoke(target, "setCreateTime", now);
//                            }
//                            if (hasField(target, "createUser")) {
//                                access.invoke(target, "setCreateUser", currentUser);
//                            }
//                        }
//
//                        if (hasField(target, "updateTime")) {
//                            access.invoke(target, "setUpdateTime", now);
//                        }
//
//                        if (hasField(target, "updateUser")) {
//                            access.invoke(target, "setUpdateUser", currentUser);
//                        }
//                    }
//                }
//
//            }
//        }
//
//        return invocation.proceed();
//    }
//
//    private static Map<String, Boolean> hasCommnField = new ConcurrentHashMap<>();
//
//
//    /**
//     * 缓存 某个类的 某个字段是否存在
//     *
//     * @param param
//     * @param fieldName
//     * @return
//     */
//    private static Boolean hasField(Object param, String fieldName) {
//        if(param==null){
//            return false;
//        }
//
//        String className = param.getClass().getName();
//        String key = new StringBuilder(className).append(".").append(fieldName).toString();
//        if (hasCommnField.get(key) != null) {
//            return hasCommnField.get(key);
//        }
//        synchronized (hasCommnField) {
//            if (hasCommnField.get(key) != null) {
//                return hasCommnField.get(key);
//            }
//            Boolean exist = false;
//            while (true) {
//                try {
//                    param.getClass().getDeclaredField(fieldName);
//                    exist = true;
//                    break;
//                } catch (Exception e) {
//                }
//                try {
//                    param.getClass().getSuperclass().getDeclaredField(fieldName);
//                    exist = true;
//                    break;
//                } catch (Exception e) {
//                }
//                log.warn(key + "not exist");
//                break;
//            }
//            hasCommnField.put(key, exist);
//        }
//        return hasCommnField.get(key);
//    }
//
//
//    public Object plugin(Object target) {
//        return Plugin.wrap(target, this);
//    }
//
//    public void setProperties(Properties arg0) {
//    }
//
//}
