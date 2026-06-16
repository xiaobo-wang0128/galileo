package org.armada.galileo.mybatis.interceptor;//package org.vot.bronze.config.framwork.mybatis.interceptor;
//
//import org.vot.bronze.common.constant.YesOrNoEnum;
//import org.vot.bronze.common.model.BaseEntity;
//import org.vot.bronze.common.model.LoginUser;
//import org.vot.bronze.common.model.Pair;
//import org.vot.bronze.common.model.ThreadUser;
//import org.vot.bronze.config.framwork.mybatis.TenantHandler;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.ibatis.binding.MapperMethod;
//import org.apache.ibatis.executor.Executor;
//import org.apache.ibatis.mapping.MappedStatement;
//import org.apache.ibatis.plugin.*;
//import org.armada.galileo.common.util.CommonUtil;
//import org.armada.galileo.es.entity.EsBaseEntity;
//import org.armada.galileo.es.mapper.EsBaseMapper;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.ApplicationContext;
//
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.Properties;
//
//
//@Slf4j
//@Intercepts({@Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class})})
//public class MybatisAutoEsIndexInterceptor extends BaseMybatisInterceptor implements Interceptor {
//
//    public static final String INSERT = "INSERT";
//
//    private static Map<Class, EsBaseMapper> mapperCache = new HashMap<>();
//
//    public MybatisAutoEsIndexInterceptor(List<EsBaseMapper> esBaseMapperList) {
//
//        for (EsBaseMapper esBaseMapper : esBaseMapperList) {
//            mapperCache.put(esBaseMapper.getEntityClass(), esBaseMapper);
//        }
//
//    }
//
//    public Object intercept(Invocation invocation) throws Throwable {
//
//        Object param = invocation.getArgs()[1];
//
//        String sql = getSqlByInvocation(invocation);
//
//        boolean isInsert = sql.startsWith(INSERT);
//
//        if (param != null) {
//
//            BaseEntity entity = null;
//
//            // mybatis-plus update
//            if (param instanceof MapperMethod.ParamMap) {
//                MapperMethod.ParamMap map = (MapperMethod.ParamMap) param;
//                if (map.containsKey("et")) {
//                    Object obj = map.get("et");
//                    if (obj != null && obj instanceof BaseEntity) {
//                        entity = (BaseEntity) obj;
//                    }
//                }
//            }
//            // other update
//            else {
//                if (param instanceof BaseEntity) {
//                    entity = (BaseEntity) param;
//                }
//            }
//
//            if (entity != null) {
//
//                LoginUser user = ThreadUser.get();
//
//                // 数据插入
//                if (isInsert) {
//                    if (user != null) {
//                        entity.setCreator(user.getUserName());
//                        entity.setModifier(user.getUserName());
//                    } else {
//                        entity.setCreator("system");
//                        entity.setModifier("system");
//                    }
//                    entity.setIsDelete(YesOrNoEnum.N);
//                    entity.setGmtCreate(System.currentTimeMillis());
//                    entity.setGmtModify(System.currentTimeMillis());
//
//                }
//                // 数据库更新
//                else {
//                    if (user != null) {
//                        entity.setModifier(user.getUserName());
//                    } else {
//                        entity.setModifier("system");
//                    }
//                    entity.setGmtModify(System.currentTimeMillis());
//                }
//
//                if (entity instanceof EsBaseEntity && entity.getId() != null) {
//                    EsBaseMapper esBaseMapper = mapperCache.get(entity.getClass());
//                    if (esBaseMapper != null) {
//                        esBaseMapper.save((EsBaseEntity) entity);
//                    }
//                }
//            }
//        }
//
//        return invocation.proceed();
//    }
//
//    public Object plugin(Object target) {
//        return Plugin.wrap(target, this);
//    }
//
//    public void setProperties(Properties arg0) {
//
//    }
//
//}
