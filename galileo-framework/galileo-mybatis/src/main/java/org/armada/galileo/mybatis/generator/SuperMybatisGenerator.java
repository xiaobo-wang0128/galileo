package org.armada.galileo.mybatis.generator;

import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.baomidou.mybatisplus.generator.AutoGenerator;
import com.baomidou.mybatisplus.generator.InjectionConfig;
import com.baomidou.mybatisplus.generator.config.*;
import com.baomidou.mybatisplus.generator.config.builder.ConfigBuilder;
import com.baomidou.mybatisplus.generator.config.rules.DateType;
import com.baomidou.mybatisplus.generator.config.rules.FileType;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;
import com.baomidou.mybatisplus.generator.engine.VelocityTemplateEngine;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SuperMybatisGenerator {

    public static String driverClassName = null;
    public static String url = null;
    public static String username = null;
    public static String password = null;

//	static {
//		driverClassName = YamlReader.instance.getValueByKey("spring.datasource.driverClassName");
//		url = YamlReader.instance.getValueByKey("spring.datasource.url");
//		username = YamlReader.instance.getValueByKey("spring.datasource.username");
//		password = YamlReader.instance.getValueByKey("spring.datasource.password");
//	}


    public static String dalPath = null;
    public static String webPath = null;
    public static String servicePath = null;

    public static enum JobType {
        web,

        dal,

        service,

        spi,

        transfer
    }


    /**
     * 生成代码
     *
     * @param rootPackage 包根目录
     * @param jobType     任务类型
     * @param targetPath  目录路径
     * @param subModel    子模块名
     * @param tables      表名
     */
    public static void doGenerateCommon(String rootPackage, JobType jobType, String targetPath, String subModel, String tables) {

        String[] tableNames = tables.split(",|\\s+");

        boolean needSetSuperService = false;
        String sys = rootPackage.substring(rootPackage.lastIndexOf(".") + 1);
        for (String tableName : tableNames) {
            if (tableName.toLowerCase(Locale.ROOT).startsWith(sys)) {
                needSetSuperService = true;
                break;
            }
        }

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
        URL url = SuperMybatisGenerator.class.getResource("");
        GlobalConfig gc = new GlobalConfig();

        String userDir = url.getPath();

        String tmpSign = "/galaxy-common/common-generate/target";
        if (userDir.indexOf(tmpSign) != -1) {
            userDir = userDir.substring(0, userDir.indexOf(tmpSign));
        }

        String projectPath = userDir;
        gc.setOutputDir(projectPath + "/" + targetPath + "/src/main/java");
        gc.setAuthor("");
        gc.setOpen(false);
        gc.setDateType(DateType.ONLY_DATE);


        // 包配置
        PackageConfig pc = new PackageConfig();

        pc.setModuleName(subModel);
        pc.setParent(rootPackage);

        // 配置模板
        TemplateConfig templateConfig = new TemplateConfig();

        // 自定义配置
        InjectionConfig cfg = new InjectionConfig() {
            @Override
            public void initMap() {
            }
        };

        // 不生成如下类型模板
        if (jobType == JobType.web) {
            pc.setController("web.rpc");
            pc.setService("bo");
            pc.setServiceImpl("bo.impl");

            gc.setServiceName("%sBO");
            gc.setServiceImplName("%sBOImpl");
            gc.setControllerName("%sRpc");

            templateConfig.setController("/mybatis_template/web_rpc.java.vm");

            templateConfig.setEntity(null);
            templateConfig.setXml(null);
            templateConfig.setService(null);
            templateConfig.setServiceImpl(null);
            templateConfig.setMapper(null);
            templateConfig.setEntityKt(null);

        } else if (jobType == JobType.spi) {
            gc.setEntityName("%sDTO");
            pc.setEntity("dto");

            templateConfig.setEntity("/mybatis_template/dto.java.vm");

            templateConfig.setController(null);
            templateConfig.setXml(null);
            templateConfig.setService(null);
            templateConfig.setServiceImpl(null);
            templateConfig.setMapper(null);
            templateConfig.setEntityKt(null);

        } else if (jobType == JobType.transfer) {

            gc.setServiceName("%sTransfer");
            pc.setService("transfer");

            templateConfig.setService("/mybatis_template/transfer.java.vm");

            templateConfig.setController(null);
            templateConfig.setEntity(null);
            templateConfig.setXml(null);
            templateConfig.setServiceImpl(null);
            templateConfig.setMapper(null);
            templateConfig.setEntityKt(null);


            pc.setEntity("dal.entity");

        } else if (jobType == JobType.dal) {

            String templatePath = "/templates/mapper.xml.vm";

            List<FileOutConfig> focList = new ArrayList<>();

            // 自定义输出配置
            focList.add(new FileOutConfig(templatePath) {
                public String outputFile(com.baomidou.mybatisplus.generator.config.po.TableInfo tableInfo) {
                    String ttPath = gc.getOutputDir() + "/" + rootPackage.replaceAll("\\.", "/") + "/" + pc.getModuleName() + "/dal/mapper/" + tableInfo.getEntityName() + "Mapper" + StringPool.DOT_XML;

                    System.out.println("ttPath: " + ttPath);
                    return ttPath;
                }
            });

            cfg.setFileOutConfigList(focList);

            templateConfig.setXml(null);
            templateConfig.setService(null);
            templateConfig.setServiceImpl(null);
            templateConfig.setController(null);
            templateConfig.setEntity("/mybatis_template/entity.java.vm");


            pc.setEntity("dal.entity");
            pc.setMapper("dal.mapper");

        } else if (jobType == JobType.service) {
            gc.setServiceName("%sBO");
            gc.setServiceImplName("%sBOImpl");

            pc.setService("bo");
            pc.setServiceImpl("bo.impl");
            pc.setEntity("dal.entity");
            pc.setMapper("dal.mapper");

            templateConfig.setService("/mybatis_template/bo.java.vm");
            templateConfig.setServiceImpl("/mybatis_template/bo.impl.java.vm");

            templateConfig.setController(null);
            templateConfig.setEntity(null);
            templateConfig.setXml(null);
            templateConfig.setMapper(null);
            templateConfig.setEntityKt(null);

        }

        mpg.setPackageInfo(pc);

        if (jobType == JobType.spi) {
            cfg.setFileCreate(new IFileCreate() {
                @Override
                public boolean isCreate(ConfigBuilder configBuilder, FileType fileType, String filePath) {
                    if (fileType == FileType.ENTITY || fileType == FileType.MAPPER || fileType == FileType.XML || fileType == FileType.SERVICE || fileType == FileType.SERVICE_IMPL || fileType == FileType.CONTROLLER) {
                        return !new File(filePath).exists();
                    }
                    return true;
                }
            });
        } else {
            cfg.setFileCreate(new IFileCreate() {
                @Override
                public boolean isCreate(ConfigBuilder configBuilder, FileType fileType, String filePath) {
                    if (filePath.endsWith(".xml")) {
                        return !new File(filePath).exists();
                    }
                    if (fileType == FileType.MAPPER || fileType == FileType.XML || fileType == FileType.SERVICE || fileType == FileType.SERVICE_IMPL || fileType == FileType.CONTROLLER) {
                        return !new File(filePath).exists();
                    }
                    return true;
                }
            });
        }


        mpg.setCfg(cfg);
        mpg.setGlobalConfig(gc);


        mpg.setTemplate(templateConfig);

        // 策略配置
        StrategyConfig strategy = new StrategyConfig();
        strategy.setNaming(NamingStrategy.underline_to_camel);
        strategy.setColumnNaming(NamingStrategy.underline_to_camel);

        // 所有实体类的基类， 等代码全部迁移完成，测试通过后 再调整
        if (jobType == JobType.dal) {
            strategy.setSuperEntityClass("org.vot.common.common.model.BaseEntity");
        } else {
            strategy.setSuperEntityClass("org.vot.common.common.model.BaseDTO");
        }

        strategy.setSuperEntityColumns("id", "gmt_create", "gmt_modify", "creator", "modifier", "is_delete", "tenant_id");

        strategy.setEntityLombokModel(true);
        strategy.setRestControllerStyle(false);
        strategy.setInclude(tableNames);
        strategy.setChainModel(true);

        // 生成的文件去除表前缀
        if (jobType == JobType.web ) {
            if (needSetSuperService) {
                String system = rootPackage.substring(rootPackage.lastIndexOf(".") + 1);
                strategy.setTablePrefix(system + "_");
                strategy.setSuperServiceClass(system.substring(0, 1).toUpperCase(Locale.ROOT) + system.substring(1));
            } else {
                strategy.setTablePrefix("");
                strategy.setSuperServiceClass("");
            }
        }


        mpg.setStrategy(strategy);

        mpg.setTemplateEngine(new VelocityTemplateEngine());
        mpg.execute();
    }

}
