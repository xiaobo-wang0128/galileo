package org.armada.galileo.mybatis.mysql_auto;

import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.armada.galileo.common.util.CommonUtil;
import org.armada.galileo.mybatis.annotation.TableIndex;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author xiaobo
 * @date 2023/4/22 08:54
 */
@Slf4j
public class AutoCreateTableIndex {

    private Set<Class<?>> entityList;

    private Connection conn;

    private String dbName;

    public AutoCreateTableIndex(Set<Class<?>> entityList, Connection conn, String dbName) {
        this.entityList = entityList;
        this.conn = conn;
        this.dbName = dbName;
    }

    public boolean doAutoCreateUpdateIndex() {

        boolean success = true;

        if (CommonUtil.isEmpty(entityList)) {
            return success;
        }

        //自动更新表索引
        Function<Class, List<Index>> indexFunction = aClass -> {

            if (!aClass.isAnnotationPresent(TableIndex.class)) {
                return null;
            }
            TableIndex tableIndex = (TableIndex) aClass.getAnnotation(TableIndex.class);
            List<Index> result = Arrays.stream(tableIndex.value()).map(e -> {
                return new Index(e.value(), e.unique(), e.fullIndex());

            }).collect(Collectors.toList());
            return result;
        };

        List<Index> allExsitIndex = selectIndexFromDB(conn, dbName);

        Map<String, List<Index>> existTableIndex = allExsitIndex.stream().collect(Collectors.groupingBy(e -> e.tableName));

        for (Class<?> aClass : entityList) {

            if (aClass == null) {
                continue;
            }
            String clsName = aClass.getName();
            String tableName = CommonUtil.convertJavaField2DB(clsName.substring(clsName.lastIndexOf(".") + 1));

            // 代码中定义的索引
            List<Index> projectIndexList = new ArrayList<>();

            Class<?> baseCls = aClass;
            List<Class<?>> classList = new ArrayList<>();
            while (true) {
                classList.add(baseCls);
                baseCls = baseCls.getSuperclass();
                if (baseCls == null || baseCls.getName().equals("java.lang.Object")) {
                    break;
                }
            }

            Collections.reverse(classList);


            for (Class<?> tmpCls : classList) {
                List<Index> tmpList = indexFunction.apply(tmpCls);
                if (CommonUtil.isNotEmpty(tmpList)) {
                    projectIndexList.addAll(tmpList);
                }
            }


            // 表中已存在的索引
            List<Index> existIndex = existTableIndex.get(tableName);

            // 需要删除的索引
            List<Index> dropIndex = null;

            if (CommonUtil.isNotEmpty(existIndex)) {
                dropIndex = existIndex.stream().filter(e -> {
                    for (Index index : projectIndexList) {
                        if (index.equals(e)) {
                            return false;
                        }
                    }
                    return true;
                }).collect(Collectors.toList());
            }

            // 需要添加的索引
            List addindexList = projectIndexList.stream().filter(e -> {
                if (CommonUtil.isNotEmpty(existIndex)) {
                    for (Index index : existIndex) {
                        if (index.equals(e)) {
                            return false;
                        }
                    }
                }
                return true;
            }).collect(Collectors.toList());

            try {
                // step1 删除索引
                dropIndex(conn, tableName, dropIndex);

                // step2 添加索引
                addIndex(conn, tableName, addindexList);

            } catch (Exception e) {
                success = false;
                log.error("sql error: " + e.getMessage());
            }
        }

        return success;
    }


    private void dropIndex(Connection conn, String tableName, List<Index> indexList) {
        if (CommonUtil.isEmpty(indexList)) {
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("ALTER TABLE ").append(tableName).append("\n");

        indexList = indexList.stream().distinct().collect(Collectors.toList());

        int alterTime = 0;
        for (int i = 0; i < indexList.size(); i++) {
            String index = indexList.get(i).getIndexName();

            // 阿里云rds 自动创建的索引无法删除
            if (index.indexOf("#alibaba") != -1) {
                continue;
            }

            sb.append("\tDROP INDEX ").append(index);

            if (i < indexList.size() - 1) {
                sb.append(",\n");
            } else {
                sb.append(";\n");
            }
            alterTime++;
        }

        if (alterTime == 0) {
            return;
        }

        log.info("drop table index: \n" + sb.toString());
        String sql = sb.toString();
        try {
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            log.error("sql error: " + e.getMessage());
            throw new RuntimeException("del index error: " + e.getMessage());
        }
    }


    //
//    ALTER TABLE my_table
//    ADD INDEX uniq_index1 (id),
//    ADD INDEX uniq_index2 (name),
//    ADD INDEX uniq_index3 (age);
// ADD FULLTEXT INDEX search_keyword_IDX (search_keyword) WITH PARSER ngram;
//
    private void addIndex(Connection conn, String tableName, List<Index> indexList) {

        if (CommonUtil.isEmpty(indexList)) {
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("ALTER TABLE ").append(tableName).append("\n");

        for (int i = 0; i < indexList.size(); i++) {
            Index index = indexList.get(i);

            if (index.fullIndex) {
                sb.append(CommonUtil.format("\tADD FULLTEXT INDEX {} ({}) WITH PARSER ngram", index.getIndexName(), CommonUtil.join(index.getColumns(), ",")));
            } else if (index.unique) {
                sb.append(CommonUtil.format("\tADD UNIQUE INDEX {} ({})  USING BTREE", index.getIndexName(), CommonUtil.join(index.getColumns(), ",")));
            } else {
                sb.append(CommonUtil.format("\tADD INDEX {} ({})", index.getIndexName(), CommonUtil.join(index.getColumns(), ",")));
            }

            if (i < indexList.size() - 1) {
                sb.append(",\n");
            } else {
                sb.append(";\n");
            }
        }

        log.info("alter table index: \n" + sb.toString());
        String sql = sb.toString();
        try {
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            log.error("*********************************************");
            log.error("************* sql 变更异常，请检查 *************");
            log.error("*********************************************");
            log.error("sql error: " + e.getMessage());

            // throw new RuntimeException("add index error: " + e.getMessage());

            System.exit(0);
        }
    }


    public List<Index> selectIndexFromDB(Connection conn, String db) {

        String sql = "select t1.NAME index_name, t1.type type, t2.NAME table_name from " + "information_schema.INNODB_INDEXES  t1," + "(" + "select TABLE_ID, NAME from information_schema.INNODB_TABLES where NAME like '" + db + "/%'" + ") t2 " + "where t1.TABLE_ID = t2.TABLE_ID";

        PreparedStatement stmt;

        List<Index> result = new ArrayList<>();
        try {
            stmt = conn.prepareStatement(sql);
            log.info("[mysql-auto] query index: " + sql);
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {

                String tableName = rs.getString("table_name");
                tableName = tableName.substring(tableName.indexOf("/") + 1);
                String indexName = rs.getString("index_name");
                int mysqlIndexType = rs.getInt("type");

                if (indexName.equals("PRIMARY")) {
                    continue;
                }
                if (indexName.indexOf("FTS_DOC") != -1 || indexName.indexOf("GEN_CLUST") != -1) {
                    continue;
                }

                Index index = new Index(tableName, indexName, mysqlIndexType == 2 || mysqlIndexType == 3);

                result.add(index);
            }
        } catch (Exception e) {
            log.error("sql error: " + e.getMessage());
            throw new RuntimeException("add index error: " + e.getMessage());
        }
        return result;
    }


    @Data
    @Accessors(chain = true)
    private static class Index {

        String tableName;

        String indexName;
        String[] columns;
        boolean unique;
        boolean fullIndex;
        // 0 普通索引 2 唯一索引  3 主键  32 全文索引
        // int mysqlIndexType;

        Index(String[] columns, boolean unique, boolean fullIndex) {
            this.columns = columns;
            this.unique = unique;
            this.fullIndex = fullIndex;
            this.indexName = "idx_" + CommonUtil.join(columns, "_");
//            if (fullIndex) {
//                this.mysqlIndexType = 32;
//            } else {
//                if (unique) {
//                    mysqlIndexType = 2;
//                } else {
//                    mysqlIndexType = 0;
//                }
//            }
        }

        Index(String tableName, String indexName, boolean unique) {
            this.tableName = tableName;
            this.indexName = indexName;
            this.unique = unique;
        }


        @Override
        public String toString() {
            final StringBuffer sb = new StringBuffer("Index{");
            sb.append("indexName='").append(indexName).append('\'');
            sb.append(", unique=").append(unique);
            sb.append('}');
            return sb.toString();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Index index = (Index) o;
            return o.toString().equals(this.toString());
        }

    }

}
