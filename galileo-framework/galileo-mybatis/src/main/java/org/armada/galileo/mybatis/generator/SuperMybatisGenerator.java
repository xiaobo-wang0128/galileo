package org.armada.galileo.mybatis.generator;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.baomidou.mybatisplus.generator.AutoGenerator;
import com.baomidou.mybatisplus.generator.InjectionConfig;
import com.baomidou.mybatisplus.generator.config.DataSourceConfig;
import com.baomidou.mybatisplus.generator.config.FileOutConfig;
import com.baomidou.mybatisplus.generator.config.GlobalConfig;
import com.baomidou.mybatisplus.generator.config.IFileCreate;
import com.baomidou.mybatisplus.generator.config.PackageConfig;
import com.baomidou.mybatisplus.generator.config.StrategyConfig;
import com.baomidou.mybatisplus.generator.config.TemplateConfig;
import com.baomidou.mybatisplus.generator.config.builder.ConfigBuilder;
import com.baomidou.mybatisplus.generator.config.rules.DateType;
import com.baomidou.mybatisplus.generator.config.rules.FileType;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;
import com.baomidou.mybatisplus.generator.engine.VelocityTemplateEngine;

public class SuperMybatisGenerator {

    String driverClassName = null;
    String url = null;
    String username = null;
    String password = null;
    String rootPackagePath = "com.hairoutech.wms.core.domain.mysql";


    public SuperMybatisGenerator(String driverClassName,
                                 String url,
                                 String username,
                                 String password,
                                 String rootPackagePath) {

        this.driverClassName = driverClassName;
        this.url = url;
        this.username = username;
        this.password = password;
        this.rootPackagePath = rootPackagePath;

    }


    public void doGenerateJob(String modulePath, String subModuleName, String tables, boolean generateRpc) {

        doGenerateCommon(modulePath, subModuleName, tables, generateRpc);

        doGenerateDTO(modulePath, subModuleName, tables);
    }

    /**
     * 生成 mapper、entity、bo、web_rpc
     *
     * @param modulePath    模块相对路径
     * @param subModuleName 子模块名
     * @param tables        表名
     */
    public void doGenerateCommon(String modulePath, String subModuleName, String tables, boolean generateRpc) {

        // 路径 相关
        String rootPackage = rootPackagePath;

        String[] tableNames = tables.split(",");

        // 代码生成器
        AutoGenerator mpg = new AutoGenerator();

        // 数据源配置
        DataSourceConfig dsc = new DataSourceConfig();
        dsc.setUrl(url);
        dsc.setDriverName(driverClassName);
        dsc.setUsername(username);
        dsc.setPassword(password);

        mpg.setDataSource(dsc);

        // 全局配置
        GlobalConfig gc = new GlobalConfig();
        String userDir = System.getProperty("user.dir");
        // userDir = userDir.substring(0, userDir.lastIndexOf("/")) + "/" + modulePath;

        String projectPath = userDir + "/" + modulePath;

        gc.setOutputDir(projectPath + "/src/main/java");
        if(! new File(gc.getOutputDir()).exists()){
            throw  new RuntimeException("源码路径不存在: "  + gc.getOutputDir());
        }

        gc.setAuthor("");
        gc.setOpen(false);
        gc.setDateType(DateType.ONLY_DATE);

        gc.setServiceName("%sBO");
        gc.setServiceImplName("%sBOImpl");
        gc.setControllerName("%sRpc");
        mpg.setGlobalConfig(gc);

        // 包配置
        PackageConfig pc = new PackageConfig();
        pc.setModuleName(subModuleName);
        pc.setParent(rootPackagePath);
        pc.setEntity("dal.entity");
        pc.setMapper("dal.mapper");
        pc.setService("bo");
        pc.setServiceImpl("bo.impl");
        pc.setController("web.rpc");

        mpg.setPackageInfo(pc);

        // 自定义配置
        InjectionConfig cfg = new InjectionConfig() {
            @Override
            public void initMap() {
            }
        };

        // 如果模板引擎是 velocity
        String templatePath = "/templates/mapper.xml.vm";

        // 自定义输出配置
        List<FileOutConfig> focList = new ArrayList<>();
        focList.add(new FileOutConfig(templatePath) {
            public String outputFile(com.baomidou.mybatisplus.generator.config.po.TableInfo tableInfo) {
                return projectPath + "/src/main/java/" + rootPackage.replaceAll("\\.", "/") + "/" + pc.getModuleName() + "/dal/mapper/" + tableInfo.getEntityName() + "Mapper" + StringPool.DOT_XML;
            }
        });
        cfg.setFileCreate(new IFileCreate() {
            @Override
            public boolean isCreate(ConfigBuilder configBuilder, FileType fileType, String filePath) {
                if (fileType == FileType.MAPPER || fileType == FileType.XML || fileType == FileType.SERVICE || fileType == FileType.SERVICE_IMPL || fileType == FileType.CONTROLLER) {
                    return !new File(filePath).exists();
                }
                return true;
            }
        });

        cfg.setFileOutConfigList(focList);
        mpg.setCfg(cfg);

        // 配置模板
        TemplateConfig templateConfig = new TemplateConfig();

        // 不生成如下类型模板
        templateConfig.setXml(null);
        templateConfig.setController(null);
        templateConfig.setService("/mybatis_template/bo.java.vm");
        templateConfig.setServiceImpl("/mybatis_template/bo.impl.java.vm");
        if (generateRpc) {
            templateConfig.setController("/mybatis_template/web_rpc.java.vm");
        } else {
            templateConfig.setController(null);
        }

        mpg.setTemplate(templateConfig);

        // 策略配置
        StrategyConfig strategy = new StrategyConfig();
        strategy.setNaming(NamingStrategy.underline_to_camel);
        strategy.setColumnNaming(NamingStrategy.underline_to_camel);

        // 所有实体类的基类， 等代码全部迁移完成，测试通过后 再调整
        // strategy.setSuperEntityClass("com.haiq.train.common.base.BaseEntity");
        // strategy.setSuperEntityColumns("id", "create_time", "update_time",
        // "create_user", "update_user");

        strategy.setEntityLombokModel(true);
        strategy.setRestControllerStyle(false);
        strategy.setInclude(tableNames);
        strategy.setChainModel(true);

        // 生成的文件去除表前缀
        // strategy.setTablePrefix(pc.getModuleName() + "_");

        mpg.setStrategy(strategy);

        mpg.setTemplateEngine(new VelocityTemplateEngine());
        mpg.execute();
    }

    /**
     * 生成 DTO、 Transfer
     *
     * @param modulePath    模块相对路径
     * @param subModuleName 子模块名
     * @param tables        表名
     */
    public void doGenerateDTO(String modulePath, String subModuleName, String tables) {

        String[] tableNames = tables.split(",");

        // 代码生成器
        AutoGenerator mpg = new AutoGenerator();

        // 数据源配置
        DataSourceConfig dsc = new DataSourceConfig();
        dsc.setUrl(url);
        dsc.setDriverName(driverClassName);
        dsc.setUsername(username);
        dsc.setPassword(password);
        mpg.setDataSource(dsc);

        // 全局配置
        GlobalConfig gc = new GlobalConfig();
        String userDir = System.getProperty("user.dir");
        // userDir = userDir.substring(0, userDir.lastIndexOf("/")) + "/" + modulePath;
        String projectPath = userDir + "/" + modulePath;
        gc.setOutputDir(projectPath + "/src/main/java");
        if(! new File(gc.getOutputDir()).exists()){
            throw  new RuntimeException("源码路径不存在: "  + gc.getOutputDir());
        }

        gc.setAuthor("");
        gc.setOpen(false);
        gc.setDateType(DateType.ONLY_DATE);
        gc.setEntityName("%sDTO");
        gc.setServiceName("%sTransfer");

        mpg.setGlobalConfig(gc);

        // 包配置
        PackageConfig pc = new PackageConfig();
        pc.setModuleName(subModuleName);
        pc.setParent(rootPackagePath);
        pc.setEntity("dal.dto");
        pc.setService("dal.transfer");

        mpg.setPackageInfo(pc);

        // 自定义配置
        InjectionConfig cfg = new InjectionConfig() {
            @Override
            public void initMap() {
            }
        };

        cfg.setFileCreate(new IFileCreate() {
            @Override
            public boolean isCreate(ConfigBuilder configBuilder, FileType fileType, String filePath) {
                if (fileType == FileType.ENTITY || fileType == FileType.SERVICE) {
                    return !new File(filePath).exists();
                }
                return true;
            }
        });

        mpg.setCfg(cfg);

        // 配置模板
        TemplateConfig templateConfig = new TemplateConfig();

        // 不生成如下类型模板
        templateConfig.setEntity("/mybatis_template/dto.java.vm");
        templateConfig.setService("/mybatis_template/transfer.java.vm");
        templateConfig.setController(null);

        templateConfig.setXml(null);

        templateConfig.setServiceImpl(null);
        templateConfig.setMapper(null);
        templateConfig.setEntityKt(null);

        mpg.setTemplate(templateConfig);

        // 策略配置
        StrategyConfig strategy = new StrategyConfig();
        strategy.setNaming(NamingStrategy.underline_to_camel);
        strategy.setColumnNaming(NamingStrategy.underline_to_camel);

        // 所有实体类的基类， 等代码全部迁移完成，测试通过后 再调整
        // strategy.setSuperEntityClass("com.haiq.train.common.base.BaseEntity");
        // strategy.setSuperEntityColumns("id", "create_time", "update_time",
        // "create_user", "update_user");

        strategy.setEntityLombokModel(true);
        strategy.setRestControllerStyle(false);
        strategy.setInclude(tableNames);
        strategy.setChainModel(true);

        // 生成的文件去除表前缀
        // strategy.setTablePrefix(pc.getModuleName() + "_");

        mpg.setStrategy(strategy);

        mpg.setTemplateEngine(new VelocityTemplateEngine());
        mpg.execute();
    }

}
