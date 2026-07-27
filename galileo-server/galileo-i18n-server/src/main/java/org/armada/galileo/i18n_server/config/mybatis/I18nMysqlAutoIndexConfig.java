package org.armada.galileo.i18n_server.config.mybatis;

import lombok.extern.slf4j.Slf4j;
import org.armada.galileo.common.util.CommonUtil;
import org.armada.galileo.mybatis.mysql_auto.AutoUpdateTableJob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * @author xiaobo
 * @date 2023/4/21 16:55
 */
@Order(1)
@Component
@Slf4j
public class I18nMysqlAutoIndexConfig {

    private List<String> entityPackageList = CommonUtil.asList(
            "org.armada.galileo.i18n_server.dal.entity"
    );

    @Autowired
    ApplicationContext applicationContext;

    @Value("${spring.datasource.url}")
    private String jdbcUrl;

    @Value("${spring.datasource.username}")
    private String user;

    @Value("${spring.datasource.password}")
    private String password;

    @PostConstruct
    public void init() {
        log.info("start mysql auto job");
//        AutoUpdateTableJob autoUpdateTableJob =
//                new AutoUpdateTableJob("i18n", entityPackageList, null, jdbcUrl, user, password, true);
//        autoUpdateTableJob.doJob();
    }

}
