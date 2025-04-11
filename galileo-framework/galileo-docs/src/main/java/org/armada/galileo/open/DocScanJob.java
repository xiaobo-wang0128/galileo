package org.armada.galileo.open;

import org.armada.galileo.open.util.api_scan.domain.DocumentGenerateTask;
import org.armada.galileo.open.util.api_scan.DocGenerate;
import org.armada.galileo.open.util.i18n_scan.EnumJsonScan;
import org.armada.galileo.open.util.i18n_scan.EnumScan;
import org.armada.galileo.open.util.open_api_scan.OpenApiGenerate;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;

/**
 * @author xiaobo
 * @date 2022/12/25 12:18
 */

@SpringBootApplication(scanBasePackages = {"org.armada"})
@ImportResource
public class DocScanJob {

    public static void main(String[] args) throws Exception {
        String jobType = args[0]; // "WEBX_URL";
        String projectPath = args[1]; // "/Users/wangxiaobo/project/_codes/aml_2022/bronze.git";
        String outputFileName = args[2];  // "/Users/wangxiaobo/project/_codes/aml_2022/galileo/galileo-vue/galileo-portal-vue/src/page/system/api_sub/api.json";
        String rootPackage = args[3]; // "com.iml";


        // webx url 扫描
        if ("WEBX_URL".equals(jobType)) {
            DocumentGenerateTask task = new DocumentGenerateTask();
            task.setGenerateType(DocumentGenerateTask.GenerateType.valueOf(jobType));
            task.setRootPackage(rootPackage);
            task.setProjectRootPath(projectPath);
            task.setOutputFilePath(outputFileName);
            new DocGenerate(task).generateWebxDoc();
        }
        // 枚举 i18n
        else if ("ENUM_SCAN".equals(jobType)) {
            EnumScan.doScan(projectPath, outputFileName);
        }
        // 枚举 i18n json
        else if ("ENUM_SCAN_JSON".equals(jobType)) {
            EnumJsonScan.doScan(projectPath, outputFileName);
        }
        // open api
        else if ("OPEN_API".equals(jobType)) {

            projectPath = args[1]; // "/Users/wangxiaobo/project/_codes/aml_2022/bronze.git";
            String scanPath = args[2]; // "/Users/wangxiaobo/project/_codes/aml_2022/bronze.git";
            outputFileName = args[3];  // "/Users/wangxiaobo/project/_codes/aml_2022/galileo/galileo-vue/galileo-portal-vue/src/page/system/api_sub/api.json";
            rootPackage = args[4]; // "com.iml";

            System.out.println("开始扫描open api, path: " + scanPath);

            DocumentGenerateTask task = new DocumentGenerateTask();
            task.setGenerateType(DocumentGenerateTask.GenerateType.valueOf(jobType));
            task.setRootPackage(rootPackage);
            task.setScanPath(scanPath);
            task.setProjectRootPath(projectPath);
            task.setOutputFilePath(outputFileName);
            new OpenApiGenerate(task).generateOpenApiDoc();

        }

    }
}
