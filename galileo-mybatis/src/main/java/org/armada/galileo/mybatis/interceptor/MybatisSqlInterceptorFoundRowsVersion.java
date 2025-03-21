package org.armada.galileo.mybatis.interceptor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.reflection.DefaultReflectorFactory;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.factory.DefaultObjectFactory;
import org.apache.ibatis.reflection.wrapper.DefaultObjectWrapperFactory;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.transaction.Transaction;
import org.armada.galileo.common.page.PageList;
import org.armada.galileo.common.page.PageParam;
import org.armada.galileo.common.page.ThreadPagingUtil;
import org.armada.galileo.common.util.CommonUtil;

/**
 * mybatis mysql 分页拦截器
 * <p>
 * 利用mysql SQL_CALC_FOUND_ROWS FOUND_ROWS 实现
 *
 * @author wangxiaobo
 */

@Intercepts(@Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}))
public class MybatisSqlInterceptorFoundRowsVersion implements Interceptor {

    @Override
    public Object intercept(Invocation invocation) throws Throwable {

        PageParam param = ThreadPagingUtil.get();

        if (param != null && param.getOpenPage()) {

            try {

                int start = 0;
                Integer limit = param.getPageSize();
                if (limit == null || limit <= 0) {
                    limit = 20;
                }

                Integer currentPage = param.getPageIndex();
                if (currentPage == null) {
                    currentPage = 1;
                }

                if (currentPage > 1) {
                    start = (currentPage - 1) * limit;
                }

                String sql = getSqlByInvocation(invocation);
                if (CommonUtil.isEmpty(sql)) {
                    return invocation.proceed();
                }

                sql = sql.trim();
                if (!sql.toLowerCase().startsWith("select")) {
                    throw new RuntimeException("非查询语句不能开启 ThreadPagingUtil 分页");
                }

                sql = "select SQL_CALC_FOUND_ROWS" + sql.substring(6);

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

                resetSql2Invocation(invocation, sql);

                // 数据对象
                Object obj1 = invocation.proceed();

                if (!(obj1 instanceof List)) {
                    throw new RuntimeException("分页查询必须返回 List 对象");
                }

                List<?> list = (List<?>) obj1;

                if (list != null && list.size() > 0) {

                    Executor ce = (Executor) invocation.getTarget();

                    Transaction transaction = ce.getTransaction();
                    ResultSet rs = transaction.getConnection().createStatement().executeQuery("select FOUND_ROWS()");

                    int count = 0;
                    while (rs.next()) {
                        count = rs.getInt(1);
                        break;
                    }

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

            } finally {
                ThreadPagingUtil.clear();
            }

        }

        return invocation.proceed();

    }

    @Override
    public Object plugin(Object obj) {
        return Plugin.wrap(obj, this);
    }

    @Override
    public void setProperties(Properties arg0) {
        // doSomething
    }

    /**
     * 获取sql语句
     *
     * @param invocation
     * @return
     */

    private String getSqlByInvocation(Invocation invocation) {
        final Object[] args = invocation.getArgs();
        MappedStatement ms = (MappedStatement) args[0];
        Object parameterObject = args[1];
        BoundSql boundSql = ms.getBoundSql(parameterObject);
        return boundSql.getSql();
    }


    /**
     * 包装sql后，重置到invocation中
     *
     * @param invocation
     * @param sql
     * @throws SQLException
     */

    private void resetSql2Invocation(Invocation invocation, String sql) throws SQLException {
        final Object[] args = invocation.getArgs();
        MappedStatement statement = (MappedStatement) args[0];
        Object parameterObject = args[1];
        BoundSql boundSql = statement.getBoundSql(parameterObject);
        MappedStatement newStatement = newMappedStatement(statement, new BoundSqlSqlSource(boundSql));
        MetaObject msObject = MetaObject.forObject(newStatement, new DefaultObjectFactory(), new DefaultObjectWrapperFactory(), new DefaultReflectorFactory());
        msObject.setValue("sqlSource.boundSql.sql", sql);
        args[0] = newStatement;
    }

    private MappedStatement newMappedStatement(MappedStatement ms, SqlSource newSqlSource) {
        MappedStatement.Builder builder = new MappedStatement.Builder(ms.getConfiguration(), ms.getId(), newSqlSource, ms.getSqlCommandType());
        builder.resource(ms.getResource());
        builder.fetchSize(ms.getFetchSize());
        builder.statementType(ms.getStatementType());
        builder.keyGenerator(ms.getKeyGenerator());
        if (ms.getKeyProperties() != null && ms.getKeyProperties().length != 0) {
            StringBuilder keyProperties = new StringBuilder();
            for (String keyProperty : ms.getKeyProperties()) {
                keyProperties.append(keyProperty).append(",");
            }
            keyProperties.delete(keyProperties.length() - 1, keyProperties.length());
            builder.keyProperty(keyProperties.toString());
        }
        builder.timeout(ms.getTimeout());
        builder.parameterMap(ms.getParameterMap());
        builder.resultMaps(ms.getResultMaps());
        builder.resultSetType(ms.getResultSetType());
        builder.cache(ms.getCache());
        builder.flushCacheRequired(ms.isFlushCacheRequired());
        builder.useCache(ms.isUseCache());

        return builder.build();
    }

    // 定义一个内部辅助类，作用是包装sq
    class BoundSqlSqlSource implements SqlSource {
        private BoundSql boundSql;

        public BoundSqlSqlSource(BoundSql boundSql) {
            this.boundSql = boundSql;
        }

        @Override
        public BoundSql getBoundSql(Object parameterObject) {
            return boundSql;
        }
    }
}
