package org.armada.galileo.mybatis.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.scripting.defaults.DefaultParameterHandler;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.transaction.Transaction;
import org.armada.galileo.common.page.PageList;
import org.armada.galileo.common.page.PageParam;
import org.armada.galileo.common.page.ThreadPagingUtil;
import org.armada.galileo.common.util.CommonUtil;
import org.armada.galileo.exception.BizException;
import org.armada.galileo.model.domain.Pair;
import org.armada.galileo.mybatis.domain.DynamicTableName;
import org.armada.galileo.mybatis.domain.TenantHandler;
import org.armada.galileo.mybatis.domain.ThreadTableName;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


/**
 * mybatis mysql 分页拦截器
 * <p>
 *
 * @author wangxiaobo
 */
@Slf4j
@Intercepts(
        {
                @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}),
                @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class, CacheKey.class, BoundSql.class})
        }
)
public class MybatisPageInterceptor extends BaseMybatisInterceptor implements Interceptor {

    /**
     * 租户拦截器
     */
    private TenantHandler tenantHandler;

    /**
     * 是否开启动态表名替换
     */
    private boolean dynamicTableNameSwitch;


    /**
     * 租户拦截表名、字段映射
     * {
     * tenant_id__customer_id : {
     * 'ta': 1,
     * 'tb': 1
     * },
     * tenant_id: {
     * 'ta': 1,
     * 'tb': 1
     * }
     * }
     */
    private Map<String, List<String>> tableNamesCache;

    public MybatisPageInterceptor() {
    }


    public MybatisPageInterceptor(TenantHandler tenantHandler) {
        this(tenantHandler, false);
    }

    public MybatisPageInterceptor(TenantHandler tenantHandler, boolean dynamicTableNameSwitch) {

        this.tenantHandler = tenantHandler;
        this.dynamicTableNameSwitch = dynamicTableNameSwitch;

        tableNamesCache = new HashMap<>();
        if (tenantHandler != null && CommonUtil.isNotEmpty(tenantHandler.getTables())) {

            for (Map.Entry<String[], String[]> entry : tenantHandler.getTables().entrySet()) {
                String[] columnNames = entry.getKey();
                String[] tableNames = entry.getValue();

                for (String tableName : tableNames) {
                    List<String> exsit = tableNamesCache.get(tableName);
                    if (exsit == null) {
                        exsit = new ArrayList<>();
                        tableNamesCache.put(tableName, exsit);
                    }

                    for (String columnName : columnNames) {
                        if (!exsit.contains(columnName)) {
                            exsit.add(columnName);
                        }
                    }
                }
            }

        }

    }

    private static Map<String, Boolean> ignoreTenatMethod = new ConcurrentHashMap<>();

    @Override
    public Object intercept(Invocation invocation) throws Throwable {

        PageParam param = ThreadPagingUtil.get();

        if (param != null && param.getOpenPage()) {

            try {
                String sql = getSqlByInvocation(invocation);

                if (CommonUtil.isEmpty(sql)) {
                    return invocation.proceed();
                }

                if (dynamicTableNameSwitch) {
                    DynamicTableName dynamicTableName = ThreadTableName.get();
                    if (dynamicTableName != null) {
                        sql = sql.replace(dynamicTableName.getSourceTableName(), dynamicTableName.getTargettableName());
                    }
                }

                int start = 0;
                Integer limit = param.getPageSize();
                Integer currentPage = param.getPageIndex();

                // 需要查询总页码
                if (param.getNeedCount()) {
                    if (limit == null || limit <= 0) {
                        limit = 20;
                    }
                    if (currentPage == null) {
                        currentPage = 1;
                    }

                    if (currentPage > 1) {
                        start = (currentPage - 1) * limit;
                    }

                } else {
                    start = param.getStart();
                    limit = param.getLimit();
                }


                sql = sql.trim();
                if (!sql.toLowerCase().startsWith("select")) {
                    throw new RuntimeException("非查询语句不能开启 ThreadPagingUtil 分页");
                }

                // 租户参数过滤
                MappedStatement mappedStatement = (MappedStatement) invocation.getArgs()[0];
                Result tenantResult = tenantFilter(sql, mappedStatement.getId());
                if (tenantResult.needTenant) {
                    sql = tenantResult.getSql();
                }

                // 分页参数添加
                {
                    // sql = "select SQL_CALC_FOUND_ROWS" + sql.substring(6);
                    if (CommonUtil.isNotEmpty(param.getOrderByClause())) {
                        int oldIndex = sql.indexOf("order by");
                        if (oldIndex != -1) {
                            sql = sql.substring(0, oldIndex);
                        }
                        sql += " order by " + param.getOrderByClause().trim();
                    } else if (param.getOrderByColumn() != null && param.getOrderByType() != null) {
                        int oldIndex = sql.indexOf("order by");
                        if (oldIndex != -1) {
                            sql = sql.substring(0, oldIndex);
                        }
                        sql += " order by " + param.getOrderByColumn() + " " + param.getOrderByType();
                    }
                    sql += " limit " + start + ", " + limit;
                }


                // 填充新的sql至参数中
                resetSql2Invocation(invocation, sql);

                // 数据对象
                Object obj1 = invocation.proceed();

                if (!(obj1 instanceof List)) {
                    throw new RuntimeException("分页查询必须返回 List 对象");
                }

                List<?> list = (List<?>) obj1;


                // 需要查总数
                if (param.getNeedCount()) {

                    if (list != null && list.size() > 0) {

                        String countSql = toCountSql(sql, param);

                        log.info(countSql.replaceAll("\\s+", " "));

                        Integer count = 0;

                        // MappedStatement mappedStatement = (MappedStatement) invocation.getArgs()[0];
                        Object parameter = invocation.getArgs()[1];
                        BoundSql boundSql = mappedStatement.getBoundSql(parameter);
                        //Map<String, Object> params = (Map<String, Object>) boundSql.getParameterObject();

                        Executor ce = (Executor) invocation.getTarget();
                        Transaction transaction = ce.getTransaction();

                        Connection connection = transaction.getConnection();
                        PreparedStatement countStatement = connection.prepareStatement(countSql);
                        BoundSql countBs = copyAndNewBS(mappedStatement, boundSql, countSql);
                        DefaultParameterHandler parameterHandler = new DefaultParameterHandler(mappedStatement, boundSql.getParameterObject(), countBs);
                        parameterHandler.setParameters(countStatement);
                        ResultSet rs = countStatement.executeQuery();
                        if (rs.next()) {
                            count = (rs.getInt(1));
                        }
                        rs.close();
                        countStatement.close();
                        connection.close();

                        //
                        //

                        int pageSize = limit;

                        int left = count % pageSize;
                        int totalPage = left == 0 ? count / pageSize : count / pageSize + 1;

                        PageList<?> pageList = new PageList<>(list);

                        pageList.setTotalSize(count);
                        pageList.setPageIndex(currentPage);

                        pageList.setHasNext(currentPage < totalPage ? true : false);
                        pageList.setHasPre(currentPage > 1 ? true : false);
                        pageList.setPageSize(pageSize);
                        pageList.setTotalPage(totalPage);

                        return pageList;

                    } else {

                        PageList<?> pageList = new PageList<>(list);

                        pageList.setTotalSize(0);
                        pageList.setPageIndex(currentPage);

                        pageList.setHasNext(false);
                        pageList.setHasPre(false);
                        pageList.setPageSize(limit);
                        pageList.setTotalPage(0);

                        return pageList;
                    }


                }
                // 直接返回
                else {
                    return list;
                }

            } finally {
                ThreadPagingUtil.clear();


                if (dynamicTableNameSwitch) {
                    ThreadTableName.remove();
                }

            }

        } else {

            String sql = getSqlByInvocation(invocation);
            if (CommonUtil.isEmpty(sql)) {
                return invocation.proceed();
            }


            Object result = null;
            try {

                if (dynamicTableNameSwitch) {
                    DynamicTableName dynamicTableName = ThreadTableName.get();
                    if (dynamicTableName != null) {
                        sql = sql.replace(dynamicTableName.getSourceTableName(), dynamicTableName.getTargettableName());
                        resetSql2Invocation(invocation, sql);
                    }
                }

                // 租户参数过滤
                MappedStatement mappedStatement = (MappedStatement) invocation.getArgs()[0];
                Result tenantResult = tenantFilter(sql, mappedStatement.getId());
                if (tenantResult.needTenant) {
                    sql = tenantResult.getSql();
                    // 填充新的sql至参数中
                    resetSql2Invocation(invocation, sql);
                }

                // 数据对象
                result = invocation.proceed();

            } catch (Exception e) {

                log.error(e.getMessage(), e);
                throw new BizException(e);

            } finally {

                if (dynamicTableNameSwitch) {
                    ThreadTableName.remove();
                }

            }

            return result;
        }

    }


    // 需要拦截的查询方法， 仅支持 BaseMapper 的部分查询
    static Map<String, Byte> needTenantMethods = new ConcurrentHashMap<>();

    static {
        List<String> methodNames = CommonUtil.asList(
                "selectBatchIds",
                "selectByMap",
                "selectOne",
                "selectCount",
                "selectList",
                "selectMaps",
                "selectObjs",
                "selectById"
        );
        for (String methodName : methodNames) {
            needTenantMethods.put(methodName, (byte) 1);
        }
    }


    public Result tenantFilter(String sql, String sqlId) {

        if (tenantHandler == null) {
            return noTenat;
        }

        // 租户字段添加
        String methodName = sqlId.substring(sqlId.lastIndexOf(".") + 1);
        if (needTenantMethods.get(methodName) == null) {
            return noTenat;
        }


        int fromIndex = sql.indexOf("FROM");
        if (fromIndex == -1) {
            throw new BizException("查询sql中没有 FROM 关键字， 请不要在 sql 中使用小写关键字");
        }
        String head = sql.substring(0, fromIndex);
        String foot = sql.substring(fromIndex + 4);
        foot = foot.trim();
        foot = foot.replaceAll("=\\s+", "=");
        foot = foot.replaceAll("\\s+=", "=");

        String tableName = null;
        int spaceIndex = foot.indexOf(" ");
        if (spaceIndex == -1) {
            tableName = foot;
        } else {
            tableName = foot.substring(0, spaceIndex);
        }

        List<Pair<String, Long>> tenantColumnList = tenantHandler.getTenantColumnMap();
        if (tenantColumnList == null || tenantColumnList.isEmpty()) {
            return noTenat;
        }

        List<String> tableNameTenantColumns = tableNamesCache.get(tableName);
        if (tableNameTenantColumns == null) {
            return noTenat;
        }


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


            if (foot.indexOf(columnName + "=") != -1) {
                continue;
            }

            // 上下文中没有获取到租户 id
            if (tenantId == null) {
                continue;
            }

            if (index > 0) {
                appendSql.append(" AND ");
            }

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
        finalSql.append(head)
                .append(" FROM ")
                .append(tableName)
                .append(" WHERE");


        int whereIndex = foot.indexOf("WHERE");

        int orderByIndex = foot.indexOf("ORDER BY");

        int limitIndex = foot.indexOf("limit");
        if (limitIndex == -1) {
            limitIndex = foot.indexOf("LIMIT");
        }

        if (whereIndex != -1) {
            if (orderByIndex == -1) {
                if (limitIndex == -1) {
                    String oldWhere = foot.substring(whereIndex + 5);
                    finalSql.append(oldWhere);

                    finalSql.append(" AND ").append(appendSql);
                } else {

                    String oldWhere = foot.substring(whereIndex + 5, limitIndex);
                    String oldLimit = foot.substring(limitIndex);

                    finalSql.append(oldWhere);
                    finalSql.append(" AND ").append(appendSql).append(" ");
                    finalSql.append(oldLimit);
                }
            } else {
                String oldWhere = foot.substring(whereIndex + 5, orderByIndex);
                String oldOrderBy = foot.substring(orderByIndex);

                finalSql.append(oldWhere);
                finalSql.append("AND ").append(appendSql).append(" ");
                finalSql.append(oldOrderBy);
            }

        } else {
            if (orderByIndex == -1) {
                if (limitIndex == -1) {
                    finalSql.append(" ").append(appendSql);
                } else {
                    String oldLimit = foot.substring(limitIndex);
                    finalSql.append(" ").append(appendSql).append(" ");
                    finalSql.append(oldLimit);
                }
            } else {
                String oldOrderBy = foot.substring(orderByIndex);
                finalSql.append(" ").append(appendSql).append(" ");
                finalSql.append(oldOrderBy);
            }
        }

        // 对 sql 进行拦截
        return new Result(true, finalSql.toString());
    }


    private BoundSql copyAndNewBS(MappedStatement mappedStatement, BoundSql boundSql, String countSql) {
        //根据新的sql构建一个全新的boundsql对象，并将原来的boundsql中的各属性复制过来
        BoundSql newBs = new BoundSql(mappedStatement.getConfiguration(), countSql, boundSql.getParameterMappings(), boundSql.getParameterObject());
        for (ParameterMapping mapping : boundSql.getParameterMappings()) {
            String prop = mapping.getProperty();
            if (boundSql.hasAdditionalParameter(prop)) {
                newBs.setAdditionalParameter(prop, boundSql.getAdditionalParameter(prop));
            }
        }
        return newBs;
    }

    @Override
    public Object plugin(Object obj) {
        return Plugin.wrap(obj, this);
    }

    @Override
    public void setProperties(Properties arg0) {
        // doSomething
    }

    private String toCountSql(String sourceSql, PageParam pageParam) {

        String tmpSql = new String(sourceSql).toLowerCase().replaceAll("[\\s\\n]", " ");

        int fromIndex = tmpSql.indexOf(" from ");
        int orderByIndex = tmpSql.lastIndexOf("order by");
        int limitIndex = tmpSql.lastIndexOf("limit");

        if (fromIndex == -1) {
            throw new RuntimeException("异常sql，无法完成分页");
        }

        StringBuilder countSql = new StringBuilder("SELECT ");
        if (CommonUtil.isNotEmpty(pageParam.getCountField())) {
            countSql.append("COUNT(").append(pageParam.getCountField()).append(") ");
        } else {
            countSql.append("COUNT(*) ");
        }

        if (orderByIndex != -1) {
            countSql.append(sourceSql.substring(fromIndex, orderByIndex));
        } else {
            if (limitIndex != -1) {
                countSql.append(sourceSql.substring(fromIndex, limitIndex));
            } else {
                countSql.append(sourceSql.substring(fromIndex));
            }
        }
        return countSql.toString();

    }


//    public static void main(String[] args) {
//
//        TenantHandler tenantHandler = new TenantHandler() {
//
//            @Override
//            public Map<String[], String[]> getTables() {
//                Map<String[], String[]> map = new HashMap<>();
//                map.put(
//                        new String[]{"tenant_id", "customer_id"},
//                        new String[]{"oms_base_warehouse", "oms_inbound_receiving", "oms_outbound_order"}
//                );
//                return map;
//            }
//
//            @Override
//            public List<Pair<String, Long>> getTenantColumnMap() {
//                return CommonUtil.asList(Pair.of("tenant_id", 333L), Pair.of("customer_id", 444L));
//            }
//        };
//
//        MybatisPageInterceptor interceptor = new MybatisPageInterceptor(tenantHandler);
//
//        String
//                sql = "SELECT * FROM oms_base_warehouse ";
//        Result dd = interceptor.tenantFilter(sql, "selectCount");
//        System.out.println(dd);
//
//        sql = "SELECT * FROM oms_base_warehouse WHERE tenant_id=33 and bb='' ORDER BY aa desc";
//        dd = interceptor.tenantFilter(sql, "selectCount");
//        System.out.println(dd);
//
//        sql = "SELECT * FROM oms_base_warehouse ORDER BY aa desc";
//        dd = interceptor.tenantFilter(sql, "selectCount");
//        System.out.println(dd);
//
//
//        sql = "SELECT * FROM oms_base_warehouse WHERE customer_id=111 and tenant_id=444";
//        dd = interceptor.tenantFilter(sql, "selectCount");
//        System.out.println(dd);
//
//        sql = "SELECT * FROM oms_base_warehouse WHERE customer_id=111";
//        dd = interceptor.tenantFilter(sql, "selectCount");
//        System.out.println(dd);
//
//        sql = "SELECT * FROM oms_base_warehouse WHERE tenant_id=444 limit 100";
//        dd = interceptor.tenantFilter(sql, "selectCount");
//        System.out.println(dd);
//
//        sql = "SELECT * FROM oms_base_warehouse WHERE tenant_id=444 ORDER BY asdfasdf limit 100";
//        dd = interceptor.tenantFilter(sql, "selectCount");
//        System.out.println(dd);
//
//
//        sql = "SELECT * FROM oms_base_warehouse WHERE id=444";
//        dd = interceptor.tenantFilter(sql, "selectCount");
//        System.out.println(dd);
//    }


}
