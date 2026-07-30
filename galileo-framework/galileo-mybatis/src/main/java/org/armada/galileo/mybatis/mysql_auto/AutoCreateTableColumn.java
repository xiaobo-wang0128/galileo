package org.armada.galileo.mybatis.mysql_auto;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.extern.slf4j.Slf4j;
import org.armada.galileo.common.util.CommonUtil;
import org.armada.galileo.mybatis.annotation.Table;
import org.armada.galileo.mybatis.annotation.TableColumn;
import org.armada.galileo.mybatis.domain.BaseEntity;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author xiaobo
 * @date 2023/4/22 08:54
 */
@Slf4j
public class AutoCreateTableColumn {

    private Set<Class<?>> entityList;

    private Connection conn;

    private String dbName;

    public AutoCreateTableColumn(Set<Class<?>> entityList, Connection conn, String dbName) {
        this.entityList = entityList;
        this.conn = conn;
        this.dbName = dbName;
    }

    public boolean doJob() {

        if (CommonUtil.isEmpty(entityList)) {
            return true;
        }


        boolean success = true;

        // 数据库已存在的所有字段
        List<InnerColumn> existInnerColumns = selectColumnFromTable(
                conn,
                CommonUtil.format(
                        "select TABLE_NAME, COLUMN_NAME, IS_NULLABLE, DATA_TYPE, CHARACTER_MAXIMUM_LENGTH, NUMERIC_PRECISION, COLUMN_DEFAULT, COLUMN_COMMENT, NUMERIC_SCALE from information_schema.`COLUMNS` where TABLE_SCHEMA='{}'"
                        , dbName
                )
        );

        // 数据库已存在的表字段
        Map<String, List<InnerColumn>> existTableColumn = existInnerColumns.stream().collect(Collectors.groupingBy(e -> e.tableName));

        // 项目中定义的表
        List<InnerTable> projectInnerTables = entityList.stream().map(e -> readTableFromClass(e)).collect(Collectors.toList());

        // 填充字段注释
        Map<String, InnerColumn> existInnerColumnMap = existInnerColumns.stream().collect(Collectors.toMap(e -> e.tableName + "_____" + e.columnName, e -> e));
        for (InnerTable intable : projectInnerTables) {
            if (CommonUtil.isNotEmpty(intable.innerColumns)) {
                for (InnerColumn e : intable.innerColumns) {

                    if (CommonUtil.isNotEmpty(e.comment)) {
                        continue;
                    }

                    String key = e.tableName + "_____" + e.columnName;
                    InnerColumn existCol = existInnerColumnMap.get(key);
                    if (existCol != null && CommonUtil.isNotEmpty(existCol.comment)) {
                        e.comment = existCol.comment;
                    }
                }
            }
        }


        // 需要新增的表
        List<InnerTable> needCreateInnerTables = projectInnerTables.stream().filter(e -> existTableColumn.get(e.tableName) == null).collect(Collectors.toList());

        // 需要更新的表
        List<InnerTable> needUpdateInnerTables = projectInnerTables.stream().filter(e -> existTableColumn.get(e.tableName) != null).collect(Collectors.toList());


        // 新增表
        if (CommonUtil.isNotEmpty(needCreateInnerTables)) {
            for (InnerTable projectInnerTable : needCreateInnerTables) {

                String tableName = projectInnerTable.tableName;
                List<InnerColumn> innerColumnList = projectInnerTable.innerColumns;
                StringBuilder sb = new StringBuilder();
                sb.append(CommonUtil.format("CREATE TABLE `{}` (\n", tableName));

                for (int i = 0; i < innerColumnList.size(); i++) {
                    sb.append("\t");
                    sb.append(printColumnSql(innerColumnList.get(i)));

                    if (i != innerColumnList.size() - 1) {
                        sb.append(",\n");
                    }
//                    if (i < innerColumnList.size() - 1) {
//                    } else {
//                        sb.append("\n");
//                    }
                }

                if (projectInnerTable.extendBase) {
                    sb.append(",\n");
                    sb.append("\tPRIMARY KEY (`id`) \n");
                }

                sb.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='" + projectInnerTable.comment + "';");

                String createTableSql = sb.toString();

                try {
                    executeSql(conn, createTableSql);
                } catch (Exception e) {
                    success = false;
                    log.error(e.getMessage(), e);
                }
            }
        }


        // 更新现有表字段
        if (CommonUtil.isNotEmpty(needUpdateInnerTables)) {

            for (InnerTable projectInnerTable : needUpdateInnerTables) {

                String tableName = projectInnerTable.tableName;

                List<InnerColumn> tableExistInnerColumns = existTableColumn.get(tableName);
                List<InnerColumn> tableProjectInnerColumns = projectInnerTable.innerColumns;

                Map<String, InnerColumn> tableExistColumnMap = tableExistInnerColumns.stream().collect(Collectors.toMap(e -> e.columnName, e -> e));
                Map<String, InnerColumn> tableProjectColumnMap = tableProjectInnerColumns.stream().collect(Collectors.toMap(e -> e.columnName, e -> e));

                List<InnerColumn> addList = new ArrayList<>();
                List<InnerColumn> modifyList = new ArrayList<>();
                List<InnerColumn> dropList = new ArrayList<>();

                for (InnerColumn innerColumn : tableProjectInnerColumns) {

                    InnerColumn existInnerColumn = tableExistColumnMap.get(innerColumn.columnName);
                    // need add
                    if (existInnerColumn == null) {
                        addList.add(innerColumn);
                        continue;
                    }

                    if (!innerColumn.equals(existInnerColumn)) {
                        modifyList.add(innerColumn);
                    }
                }

                dropList = tableExistInnerColumns.stream().filter(e -> tableProjectColumnMap.get(e.columnName) == null).collect(Collectors.toList());

//                alter table ums_test_table
//                         ADD COLUMN `gmt_create333` bigint NOT NULL COMMENT '创建时间',
//                        CHANGE gmt_create gmt_create_backup decimal(16,3) NULL COMMENT '组织id',
//                        MODIFY COLUMN gmt_modify varchar(103) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL COMMENT '第三方关联平台类型3333'
//                ;
                if (addList.size() > 0 || modifyList.size() > 0 || dropList.size() > 0) {

                    StringBuilder sb = new StringBuilder();

                    sb.append(CommonUtil.format("ALTER TABLE `{}`\n", tableName));

                    for (InnerColumn innerColumn : addList) {
                        sb.append("\tADD COLUMN ").append(printColumnSql(innerColumn)).append(",\n");
                    }
                    for (InnerColumn innerColumn : modifyList) {
                        sb.append("\tMODIFY COLUMN ").append(printColumnSql(innerColumn)).append(",\n");
                    }
                    for (InnerColumn innerColumn : dropList) {
                        sb.append(CommonUtil.format("\tDROP COLUMN `{}`", innerColumn.columnName)).append(",\n");
                    }
                    String sql = sb.toString();
                    sql = sql.substring(0, sql.length() - 2) + ";\n";


                    try {
                        executeSql(conn, sql);
                    } catch (Exception e) {
                        success = false;
                        log.error(e.getMessage(), e);
                    }

                }

            }

        }

        return success;

    }


    private InnerTable readTableFromClass(Class clz) {


        List<Class<?>> classList = new ArrayList<>();

//        boolean extendBase = false;
//        if (clz.getSuperclass() != null) {
//            classList.add(clz.getSuperclass());
//            if (clz.getSuperclass().getName().equals(BaseEntity.class.getName())) {
//                extendBase = true;
//            }
//        }

        Class<?> baseCls = clz;

        boolean extendBase = false;

        while (true) {
            classList.add(baseCls);
            baseCls = baseCls.getSuperclass();
            if (baseCls == null || baseCls.getName().equals("java.lang.Object")) {
                break;
            }
            if (baseCls != null) {
                if (baseCls.getName().equals(BaseEntity.class.getName())) {
                    extendBase = true;
                }
            }
        }

        Collections.reverse(classList);

        // classList.add(clz);

        String tableName = CommonUtil.convertJavaField2DB(clz.getName().substring(clz.getName().lastIndexOf(".") + 1));

        String tableComment = "";
        Table table = (Table) clz.getAnnotation(Table.class);

        List<InnerColumn> innerColumns = new ArrayList<>();
        for (Class clazz : classList) {

            if (table != null) {
                tableComment = table.value();
            }

            for (Field field : clazz.getDeclaredFields()) {

                if (Modifier.isFinal(field.getModifiers()) || Modifier.isStatic(field.getModifiers())) {
                    continue;
                }

                String columnName = CommonUtil.convertJavaField2DB(field.getName());
                String typeName = field.getType().getName();

//                String tableName;
//                String columnName;
                String type = null;
                int len = -1;
                String comment = "";
                boolean notnull = false;
                String defaultValue = null;
                int precision = 2;


                TableColumn define = field.getAnnotation(TableColumn.class);
                if (define != null && !define.exists()) {
                    continue;
                }
                if (define != null) {
                    if (CommonUtil.isNotEmpty(define.type())) {
                        type = define.type().toLowerCase();
                    }
                    precision = define.precision();
                    if (CommonUtil.isNotEmpty(define.defaultValue())) {
                        defaultValue = define.defaultValue();
                    }
                    if (CommonUtil.isNotEmpty(define.comment())) {
                        comment = define.comment();
                    }
                    if (len == -1) {
                        len = define.len();
                    }
                    notnull = define.notNull();
                }

                if(type == null){
                    if (typeName.startsWith("java.lang")) {

                        if (typeName.endsWith("String")) {
                            type = "varchar";
                        } else if (typeName.endsWith("Long")) {
                            type = "bigint";
                        } else if (typeName.endsWith("Integer")) {
                            type = "int";
                        } else if (typeName.endsWith("Double")) {
                            type = "decimal";
                        } else {
                            String error = CommonUtil.format("请不在要 entity 中使用此类型: {},  className:{}, field: {}", typeName, clz.getName(), field.getName());
                            throw new RuntimeException(error);
                        }
                    } else {

                        if (field.getType().isEnum()) {
                            type = "varchar";
                            len = 32;
                            if (field.getType().getName().endsWith("YesOrNoEnum")) {
                                len = 1;
                            }
                        } else if (field.isAnnotationPresent(TableField.class)) {
                            type = "json";
                        } else if (typeName.indexOf("List") != -1) {
                            type = "json";
                        } else if (typeName.indexOf("BigDecimal") != -1) {
                            type = "decimal";
                        } else if (Date.class.getName().equals(typeName)) {
                            type = "datetime";
                        } else {
                            String error = CommonUtil.format("请不在要 entity 中使用此类型: {},  className:{}, field: {}， 如果是对象类型需要添加 @TableField 注解 "
                                    , typeName.substring(typeName.lastIndexOf(".") + 1)
                                    , clz.getName().substring(clz.getName().lastIndexOf(".") + 1)
                                    , field.getName());
                            throw new RuntimeException(error);
                        }
                    }
                }



                if (len == -1) {
                    len = 255;
                }
                // decimal 长度取 decimalLen（默认 16），不要沿用 varchar 的 255
                if ("decimal".equals(type)) {
                    if (define != null) {
                        len = define.decimalLen();
                    } else {
                        len = 16;
                    }
                }
                InnerColumn innerColumn = new InnerColumn();
                innerColumn.tableName = tableName;
                innerColumn.columnName = columnName;
                innerColumn.type = type;
                innerColumn.len = len;
                innerColumn.comment = comment;
                innerColumn.notnull = notnull;
                innerColumn.defaultValue = defaultValue;
                innerColumn.precision = precision;

                innerColumns.add(innerColumn);
            }

        }

        InnerTable innerTable = new InnerTable();
        innerTable.tableName = tableName;
        innerTable.comment = tableComment;
        innerTable.innerColumns = innerColumns;
        innerTable.extendBase = extendBase;
        return innerTable;
    }

    private String printColumnSql(InnerColumn innerColumn) {
        if ("bigint".equals(innerColumn.type) || "int".equals(innerColumn.type)) {
            return CommonUtil.format("`{}` {} {} COMMENT '{}'",
                    innerColumn.columnName,
                    innerColumn.type,
                    printDefault(innerColumn),
                    innerColumn.comment);
        }
        if ("decimal".equals(innerColumn.type)) {
            return CommonUtil.format("`{}` decimal({},{}) {} COMMENT '{}'",
                    innerColumn.columnName,
                    innerColumn.len,
                    innerColumn.precision,
                    printDefault(innerColumn),
                    innerColumn.comment);
        }
        if ("varchar".equals(innerColumn.type)) {
            return CommonUtil.format("`{}` varchar({}) {} COMMENT '{}'",
                    innerColumn.columnName,
                    innerColumn.len,
                    printDefault(innerColumn),
                    innerColumn.comment);
        }
        if ("json".equals(innerColumn.type)) {
            return CommonUtil.format("`{}` json {} COMMENT '{}'",
                    innerColumn.columnName,
                    printDefault(innerColumn),
                    innerColumn.comment);
        }


        return CommonUtil.format("`{}` {} {} COMMENT '{}'",
                innerColumn.columnName,
                innerColumn.type,
                printDefault(innerColumn),
                innerColumn.comment);
    }

    private String printDefault(InnerColumn column) {
        if (column.notnull) {
            if (CommonUtil.isNotEmpty(column.defaultValue)) {
                return "NOT NULL DEFAULT '" + column.defaultValue.trim() + "'";
            } else {
                return "NOT NULL";
            }
        } else {
            if (CommonUtil.isNotEmpty(column.defaultValue)) {
                return "DEFAULT '" + column.defaultValue.trim() + "'";
            } else {
                return "DEFAULT NULL";
            }
        }
    }

    public void executeSql(Connection conn, String sql) {
        log.info("do auto sql script:\n" + sql);
        PreparedStatement stmt;
        try {
            stmt = conn.prepareStatement(sql);
            stmt.executeUpdate(sql);
        } catch (Exception e) {
            log.error("sql execute error, sql: \n" + sql, e);
            throw new RuntimeException("sql execute error, " + e.getMessage());
        }
    }


    public List<InnerColumn> selectColumnFromTable(Connection conn, String sql) {
        PreparedStatement stmt;

        List<InnerColumn> result = new ArrayList<>();
        try {
            stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                InnerColumn col = new InnerColumn();
                // TABLE_NAME,COLUMN_NAME,IS_NULLABLE,DATA_TYPE,CHARACTER_MAXIMUM_LENGTH,NUMERIC_PRECISION,COLUMN_DEFAULT,COLUMN_COMMENT,NUMERIC_SCALE
                col.tableName = rs.getString("TABLE_NAME");
                col.columnName = rs.getString("COLUMN_NAME");
                col.type = rs.getString("DATA_TYPE");
                col.comment = rs.getString("COLUMN_COMMENT");
                col.notnull = !"YES".equals(rs.getString("IS_NULLABLE"));
                col.defaultValue = rs.getString("COLUMN_DEFAULT");
                col.precision = readIntColumn(rs, "NUMERIC_SCALE");

                // decimal/float/double 用 NUMERIC_PRECISION；其它字符类型用 CHARACTER_MAXIMUM_LENGTH
                if ("decimal".equals(col.type) || "float".equals(col.type) || "double".equals(col.type)) {
                    col.len = readIntColumn(rs, "NUMERIC_PRECISION");
                } else {
                    col.len = readLongColumn(rs, "CHARACTER_MAXIMUM_LENGTH");
                }
                result.add(col);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return result;
    }


    private static class InnerTable {
        boolean extendBase;
        String tableName;
        String comment;
        List<InnerColumn> innerColumns;
    }

    private static class InnerColumn {
        String tableName;
        String columnName;
        String type;
        long len;
        String comment;
        boolean notnull;
        String defaultValue;
        int precision;

        @Override
        public String toString() {
            final StringBuffer sb = new StringBuffer("InnerColumn{");
            sb.append("columnName='").append(columnName).append('\'');
            sb.append(", type='").append(type).append('\'');
            sb.append(", notnull=").append(notnull);
            sb.append(", defaultValue='").append(normalizeDefaultValue(type, defaultValue)).append('\'');
            if ("varchar".equals(type)) {
                sb.append(", len=").append(len);
            }
            if ("decimal".equals(type)) {
                sb.append(", len=").append(len);
                sb.append(", precision=").append(precision);
            }
            //sb.append(", comment='").append(comment).append('\'');
            sb.append('}');
            return sb.toString();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            InnerColumn innerColumn = (InnerColumn) o;
            return this.toString().equals(innerColumn.toString());
        }

    }

    /**
     * information_schema 中部分数值可能超出 Integer（如 LONGTEXT 的 CHARACTER_MAXIMUM_LENGTH=4294967295）
     */
    private static int readIntColumn(ResultSet rs, String column) throws Exception {
        long value = rs.getLong(column);
        if (rs.wasNull()) {
            return 0;
        }
        if (value > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        if (value < Integer.MIN_VALUE) {
            return Integer.MIN_VALUE;
        }
        return (int) value;
    }

    private static long readLongColumn(ResultSet rs, String column) throws Exception {
        long value = rs.getLong(column);
        if (rs.wasNull()) {
            return 0;
        }
        return value;
    }

    /**
     * 统一默认值比较：MySQL 对 decimal(16,2) DEFAULT '0' 会返回 0.00，避免每次启动误判为变更
     */
    private static String normalizeDefaultValue(String type, String defaultValue) {
        if (defaultValue == null) {
            return null;
        }
        String value = defaultValue.trim();
        if (value.isEmpty()) {
            return null;
        }
        // 个别版本可能带引号
        if (value.length() >= 2 && value.startsWith("'") && value.endsWith("'")) {
            value = value.substring(1, value.length() - 1);
        }
        if ("decimal".equals(type) || "int".equals(type) || "bigint".equals(type)
                || "float".equals(type) || "double".equals(type) || "tinyint".equals(type)
                || "smallint".equals(type) || "mediumint".equals(type)) {
            try {
                return new java.math.BigDecimal(value).stripTrailingZeros().toPlainString();
            } catch (Exception ignore) {
                return value;
            }
        }
        return value;
    }


}
