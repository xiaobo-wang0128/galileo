package org.armada.galileo.open.util.api_scan.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 文档生成器任务对象
 *
 * @author xiaobo
 * @date 2021/11/2 8:36 下午
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DocumentGenerateTask {

    public static enum GenerateType {
        /**
         * 接口api
         */
        INTERFACE_API,
        /**
         * WEB_URL
         */
        WEBX_URL,

        OPEN_API
    }

    private String rootPackage = "com.iml";

    /**
     * 项目根路径
     */
    private String projectRootPath;

    /**
     * 接口扫描的子目录
     */
    private String scanPath;

    /**
     * 文档文件输出路径
     */
    private String outputFilePath;

    /**
     * 接口生成类型
     */
    private GenerateType generateType;

    /**
     * 需要输出的接口
     */
    private List<ApiClass> apis;

    /**
     * 接口描述信息
     */
    private String desc;

    @Data
    @AllArgsConstructor
    public static class ApiClass {

        /**
         * 接口名
         */
        private String clsName;

        /**
         * 文档分组名
         */
        private String groupName;

        /**
         * 调用方向
         */
        private String type;
    }

}
