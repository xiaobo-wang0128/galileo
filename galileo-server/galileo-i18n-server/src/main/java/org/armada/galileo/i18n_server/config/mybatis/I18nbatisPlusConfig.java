package org.armada.galileo.i18n_server.config.mybatis;

import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import org.armada.galileo.i18n_server.config.interceptor.MybatisSqlLogInterceptor;
import org.armada.galileo.mybatis.interceptor.MybatisSqlInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

@Configuration
@EnableTransactionManagement
@MapperScan(basePackages = "org.armada.galileo.i18n_server.dal.mapper")
public class I18nbatisPlusConfig {


    @Autowired
    private DataSource dataSource;


    @Bean(name = "mysqlTransactionManager")
    @Primary
    public PlatformTransactionManager transactionManager() {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean(name = "sqlSessionFactory")
    public MybatisSqlSessionFactoryBean sqlSessionFactoryBean() throws Exception {

        MybatisSqlSessionFactoryBean factory = new MybatisSqlSessionFactoryBean();

        factory.setDataSource(dataSource);
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        factory.setMapperLocations(resolver.getResources("classpath*:org.armada.galileo.i18n_server.dal.mapper/*.xml"));
        factory.setPlugins(
                new MybatisSqlInterceptor(),
                new MybatisSqlLogInterceptor()
        );
        GlobalConfig globalConfig = new GlobalConfig();
        globalConfig.setMetaObjectHandler(new I18nMetaObjectHandler());
        factory.setGlobalConfig(globalConfig);
        return factory;
    }


}
