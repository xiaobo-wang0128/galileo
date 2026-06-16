package org.armada.galileo.mybatis.domain;


public class ThreadTableName {

    private static ThreadLocal<DynamicTableName> local = new ThreadLocal<DynamicTableName>();

    public static void set(DynamicTableName tableName) {
        local.set(tableName);
    }

    public static DynamicTableName get() {
        return local.get();
    }

    public static void remove() {
        local.remove();
    }

}
