package org.armada.galileo.mybatis.interceptor;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.binding.MapperMethod;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;
import org.armada.galileo.common.util.CommonUtil;
import org.armada.galileo.es.entity.EsBaseEntity;
import org.armada.galileo.es.mapper.EsBaseMapper;
import org.armada.galileo.exception.BizException;
import org.armada.galileo.model.constant.YesOrNoEnum;
import org.armada.galileo.model.domain.LoginUser;
import org.armada.galileo.model.domain.Pair;
import org.armada.galileo.model.domain.ThreadUser;
import org.armada.galileo.mybatis.domain.BaseEntity;
import org.armada.galileo.mybatis.domain.TenantHandler;

import java.util.*;

@Slf4j
@Intercepts({@Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class})})
public class MybatisUpdateInterceptor extends BaseMybatisInterceptor implements Interceptor {

    public static final String INSERT = "INSERT";

    private TenantHandler tenantHandler;

    private Map<String, List<String>> tableNamesCache;

    private static Map<Class, EsBaseMapper> mapperCache = new HashMap<>();

    public MybatisUpdateInterceptor(List<EsBaseMapper> esBaseMapperList, TenantHandler tenantHandler) {
        this(tenantHandler);
        for (EsBaseMapper esBaseMapper : esBaseMapperList) {
            mapperCache.put(esBaseMapper.getEntityClass(), esBaseMapper);
        }
    }

    public MybatisUpdateInterceptor() {
    }

    public MybatisUpdateInterceptor(TenantHandler tenantHandler) {
        this.tenantHandler = tenantHandler;
        tableNamesCache = new HashMap<>();

        for (Map.Entry<String[], String[]> entry : tenantHandler.getTables().entrySet()) {
            String[] columnNames = entry.getKey();
            String[] tableNames = entry.getValue();

            for (String tableName : tableNames) {
                tableNamesCache.put(tableName, CommonUtil.asList(columnNames));
            }
        }
    }


    public Result tenantFilter(String sql) {

        int whereIndex = sql.lastIndexOf("WHERE");

        if (whereIndex == -1) {
            throw new BizException("更新/删除语句中必须包含 WHERE 关键字（区分大小写）: " + sql);
        }
        if (tenantHandler == null) {
            return noTenat;
        }

        String tableName = CommonUtil.substr(sql, "UPDATE", "SET");

        if (tableName == null) {
            tableName = CommonUtil.substr(sql, "DELETE FROM", "WHERE");
        }
        if (tableName == null) {
            throw new BizException("更新语句中必须要有 UPDATE SET 关键字（区分大小写）: " + sql);
        }

        tableName = tableName.trim();

        List<Pair<String, Long>> tenantColumnList = tenantHandler.getTenantColumnMap();
        if (tenantColumnList == null || tenantColumnList.isEmpty()) {
            return noTenat;
        }

        List<String> tableNameTenantColumns = tableNamesCache.get(tableName);
        if (tableNameTenantColumns == null) {
            return noTenat;
        }


        String endSql = sql.substring(whereIndex + 5);

        // 追回的租户 sql 查询
        StringBuilder appendSql = new StringBuilder();
        int index = 0;
        for (Pair<String, Long> pair : tenantColumnList) {

            String columnName = pair.getLeft();
            Long tenantId = pair.getRight();

            if (!tableNameTenantColumns.contains(columnName)) {
                continue;
            }

            // sql 中已经出现了租户字段的查询条件
            if (endSql.indexOf(columnName + " =") != -1 || endSql.indexOf(columnName + "=") != -1) {
                continue;
            }

            // 上下文中没有获取到租户 id
            if (tenantId == null) {
                continue;
            }
            appendSql.append(" AND ");
            appendSql
                    .append(columnName)
                    .append("=")
                    .append(tenantId)
            ;
            index++;
        }

        if (index == 0) {
            return noTenat;
        }

        StringBuilder finalSql = new StringBuilder();
        finalSql.append(sql)
                .append(appendSql.toString())
        ;

        return new Result(true, finalSql.toString());

    }

    public Object intercept(Invocation invocation) throws Throwable {

        Object param = invocation.getArgs()[1];

        String sql = getSqlByInvocation(invocation);

        boolean isInsert = sql.startsWith(INSERT);

        if (!isInsert) {

            Result tenantResult = tenantFilter(sql);

            if (tenantResult.needTenant) {
                sql = tenantResult.getSql();

                // 填充新的sql至参数中
                resetSql2Invocation(invocation, sql);
            }

        }
        List<BaseEntity> baseEntityList = new ArrayList<>();
        // 处理entity赋值
        processEntity(isInsert, param, baseEntityList, sql);

        Object obj = invocation.proceed();

        // 同步 es
        for (BaseEntity entity : baseEntityList) {
            if (entity instanceof EsBaseEntity && entity.getId() != null) {
                EsBaseMapper esBaseMapper = mapperCache.get(entity.getClass());
                if (esBaseMapper != null) {
                    esBaseMapper.save((EsBaseEntity) entity);
                }
            }
        }
        // log.info("after invocation.proceed()");
        return obj;
    }

    private void processEntity(boolean isInsert, Object param, List<BaseEntity> entityList, String sql) {

        if (param != null) {
            // mybatis-plus update
            if (param instanceof MapperMethod.ParamMap) {
                MapperMethod.ParamMap map = (MapperMethod.ParamMap) param;
                if (map.containsKey("et")) {
                    Object obj = map.get("et");
                    if (obj instanceof BaseEntity) {
                        entityList.add((BaseEntity) obj);
                    }
                } else if (map.containsKey("list")) {
                    Object obj = map.get("list");
                    if (obj != null) {
                        List list = (List) obj;
                        for (Object o : list) {
                            if (o instanceof BaseEntity) {
                                entityList.add((BaseEntity) o);
                            }
                        }
                    }
                }
            }
            // other update
            else {
                if (param instanceof BaseEntity) {
                    entityList.add((BaseEntity) param);
                }
            }

            if (CollectionUtils.isNotEmpty(entityList)) {
                LoginUser user = ThreadUser.get();
                for (BaseEntity entity : entityList) {
                    // 数据插入
                    if (isInsert) {
                        if (user != null && StringUtils.isNotEmpty(user.getUserName())) {
                            if (entity.getCreator() == null) {
                                entity.setCreator(user.getUserName());
                            }
                            entity.setModifier(user.getUserName());
                        } else {
                            if (entity.getCreator() == null) {
                                entity.setCreator("system");
                            }
                            entity.setModifier("system");
                        }
                        entity.setIsDelete(YesOrNoEnum.N);
                        entity.setGmtCreate(System.currentTimeMillis());
                        entity.setGmtModify(System.currentTimeMillis());
                        if (entity.getTenantId() == null) {
                            if (user == null || user.getTenantId() == null) {
                                log.info("insert error:" + sql);
                                throw new BizException("新增数据时，当前用户tenantId不为能空");
                            }
                            entity.setTenantId(user.getTenantId());
                        }
                    }
                    // 数据库更新
                    else {
                        entity.setGmtCreate(null);
                        entity.setCreator(null);
                        if (user != null && StringUtils.isNotEmpty(user.getUserName())) {
                            entity.setModifier(user.getUserName());
                        } else {
                            entity.setModifier("system");
                        }

                        entity.setGmtModify(System.currentTimeMillis());
                    }
                }
            }
        }
    }

    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    public void setProperties(Properties arg0) {

    }

}
