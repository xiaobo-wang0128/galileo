package org.armada.galileo.mybatis.mysql_auto;

import lombok.extern.slf4j.Slf4j;
import org.armada.galileo.common.redis.CacheType;
import org.armada.galileo.common.redis.RedisSyncLock;
import org.armada.galileo.common.redis.RedisUtil;
import org.armada.galileo.common.util.CommonUtil;
import org.armada.galileo.common.util.JsonUtil;
import org.armada.galileo.mybatis.annotation.Table;
import org.armada.galileo.mybatis.domain.BaseEntity;
import org.reflections.Reflections;
import org.reflections.Store;
import org.reflections.util.QueryFunction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import javax.sql.DataSource;
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

    @Autowired
    private RedisSyncLock redisSyncLock;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private DataSource ds;

    @Value("${spring.datasource.url}")
    private String jdbcUrl;

    @Value("${spring.datasource.username}")
    private String user;

    @Value("${spring.datasource.password}")
    private String password;

    private String systemCode;

    private List<String> entityPackageList;

    /**
     * 自动新增、更新表结构
     */
    private boolean autoCreateUpdateTable = false;

    private static enum CommonCacheType implements CacheType {
        AutoCreateTable
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

    public void setUp(List<String> entityPackageList, String systemCode, boolean autoCreateUpdateTable) {
        this.entityPackageList = entityPackageList;
        this.systemCode = systemCode;
        this.autoCreateUpdateTable = autoCreateUpdateTable;
    }

    public void setUp(List<String> entityPackageList, String systemCode) {
        this.entityPackageList = entityPackageList;
        this.systemCode = systemCode;
    }

    public void doJob() {

        if (CommonUtil.isEmpty(entityPackageList)) {
            return;
        }

//        log.info("sun.java.command: " + (String) System.getProperties().get("sun.java.command"));
//
//        if (CommonUtil.isSpringApp()) {
//            log.info("is spring");
//            entityPackageList = entityPackageList.stream().map(e -> "BOOT-INF.classes." + e).collect(Collectors.toList());
//        } else {
//            log.info("is java ");
//        }

        // 判断当前版本的代码是否已经执行过"自动添加索引"任务了
        if (currentSystemVersion != null) {
            String v = redisUtil.get(CommonCacheType.AutoCreateTable, systemCode + "_" + currentSystemVersion);
            if (v != null) {
                log.info("[mysql auto job] version :{} has been loaded", currentSystemVersion);
                return;
            }
        }

        if (currentSystemVersion != null) {
            if (!redisSyncLock.lock(CommonCacheType.AutoCreateTable, systemCode + currentSystemVersion)) {
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

            if (currentSystemVersion != null && columnSuccess && indexSuccess) {
                redisUtil.set(CommonCacheType.AutoCreateTable, systemCode + "_" + currentSystemVersion, "success");
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

            redisSyncLock.unlock(CommonCacheType.AutoCreateTable, systemCode + currentSystemVersion);
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
