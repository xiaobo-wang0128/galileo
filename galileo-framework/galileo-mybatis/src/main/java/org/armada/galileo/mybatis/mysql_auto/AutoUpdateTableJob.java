package org.armada.galileo.mybatis.mysql_auto;

import lombok.extern.slf4j.Slf4j;
import org.armada.galileo.common.redis.CacheType;
import org.armada.galileo.common.redis.RedisSyncLock;
import org.armada.galileo.common.util.CommonUtil;
import org.armada.galileo.common.util.JsonUtil;
import org.armada.galileo.mybatis.annotation.Table;
import org.armada.galileo.mybatis.domain.BaseEntity;
import org.reflections.Reflections;
import org.reflections.Store;
import org.reflections.util.QueryFunction;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author xiaobo
 * @date 2023/4/21 16:22
 */

@Slf4j
public class AutoUpdateTableJob {

    private RedisSyncLock syncLock;

    private String jdbcUrl;

    private String user;

    private String password;

    private String systemCode;

    private List<String> entityPackageList;

    /**
     * 自动新增、更新表结构
     */
    private boolean autoCreateUpdateTable = false;

    private AutoUpdateTableJob() {
    }

    public AutoUpdateTableJob(
            String systemCode,
            List<String> entityPackageList,
            RedisSyncLock syncLock,
            String jdbcUrl, String user, String password,
            boolean autoCreateUpdateTable) {
        this.syncLock = syncLock;
        this.jdbcUrl = jdbcUrl;
        this.user = user;
        this.password = password;
        this.systemCode = systemCode;
        this.entityPackageList = entityPackageList;
        this.autoCreateUpdateTable = autoCreateUpdateTable;
    }

    private static String currentSystemVersion = null;

    {
        try {
            byte[] bufs = CommonUtil.readFileToBuffer("galaxy_version");
            if (bufs != null) {
                currentSystemVersion = new String(bufs, StandardCharsets.UTF_8);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }


    private static enum MysqlCacheType implements CacheType {
        AutoCreateTable
    }

    public void doJob() {

        if (CommonUtil.isEmpty(entityPackageList)) {
            return;
        }

        // 判断当前版本的代码是否已经执行过"自动添加索引"任务了
        if (currentSystemVersion != null && syncLock != null) {
            String v = syncLock.get(MysqlCacheType.AutoCreateTable, systemCode + "_" + currentSystemVersion);
            if (v != null) {
                log.info("[mysql auto job] version :{} has been loaded", currentSystemVersion);
                return;
            }
            if (!syncLock.lock(MysqlCacheType.AutoCreateTable, systemCode + currentSystemVersion)) {
                return;
            }
        }


        Connection conn = null;
        try {
            Set<Class<?>> entityClassList = scanEntityClass(entityPackageList);

            List<String> names = entityClassList.stream().map(e -> {
                String cname = e.getName();
                cname = cname.substring(cname.lastIndexOf(".") + 1);
                return cname;
            }).collect(Collectors.toList());

            log.info("[mysql-auto] scan class: " + JsonUtil.toJson(names));

            conn = getConnection();

            String dbName = jdbcUrl.substring(0, jdbcUrl.lastIndexOf("?"));
            dbName = dbName.substring(dbName.lastIndexOf("/") + 1);


            boolean columnSuccess = true;
            boolean indexSuccess = true;

            //自动更新表结构
            if (this.autoCreateUpdateTable) {
                AutoCreateTableColumn autoColumn = new AutoCreateTableColumn(entityClassList, conn, dbName);
                columnSuccess = autoColumn.doJob();
            }

            // 自动创建更新索引
            AutoCreateTableIndex autoIndex = new AutoCreateTableIndex(entityClassList, conn, dbName);
            indexSuccess = autoIndex.doAutoCreateUpdateIndex();

            if (currentSystemVersion != null && columnSuccess && indexSuccess && syncLock != null) {
                syncLock.set(MysqlCacheType.AutoCreateTable, systemCode + "_" + currentSystemVersion, "success");
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
            if (syncLock != null) {
                syncLock.unlock(MysqlCacheType.AutoCreateTable, systemCode + currentSystemVersion);
            }

        }

    }

    private Set<Class<?>> scanEntityClass(List<String> pathList) {
        Set<Class<?>> classes = new HashSet<>();
        for (String packagePath : pathList) {
            Reflections reflections = new Reflections(packagePath);

            QueryFunction<Store, String> query = store -> {
                Set<String> sets = new HashSet<>();

                try {
                    Map<String, Set<String>> subTypes = store.get("SubTypes");

                    if (CommonUtil.isNotEmpty(subTypes)) {
                        Set<String> tmpSet = subTypes.get(BaseEntity.class.getName());
                        if (CommonUtil.isNotEmpty(tmpSet)) {
                            for (String clsName : tmpSet) {
                                sets.add(clsName);
                                if (clsName.equals("SuperCheckDevice")) {
                                    log.info("xxx");
                                }
                            }
                        }
                    }

                    Map<String, Set<String>> typesAnnotated = store.get("TypesAnnotated");
                    if (CommonUtil.isNotEmpty(typesAnnotated)) {
                        Set<String> tmpSet = typesAnnotated.get(Table.class.getName());
                        if (CommonUtil.isNotEmpty(tmpSet)) {
                            for (String clsName : tmpSet) {
                                sets.add(clsName);
                                if (clsName.equals("SuperCheckDevice")) {
                                    log.info("xxx");
                                }
                            }
                        }
                    }

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    throw new RuntimeException("san class error: " + e.getMessage());
                }

                return sets;
            };

            Set<String> classList = reflections.get(query);

            if (classList == null || classList.isEmpty()) {
                continue;
            }

            if (CommonUtil.isNotEmpty(classList)) {
                classList = classList.stream().filter(e -> e.startsWith(packagePath)).collect(Collectors.toSet());
            }


            for (String s : classList) {
                try {
                    classes.add(Class.forName(s));
                } catch (Exception e) {
                }
            }

        }
        return classes;
    }


    private Connection getConnection() {
        Connection conn = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            String url = jdbcUrl;
            conn = DriverManager.getConnection(url, user, password);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException("add index error: " + e.getMessage());
        }
        return conn;
    }


}
