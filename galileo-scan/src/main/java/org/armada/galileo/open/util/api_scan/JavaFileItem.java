package org.armada.galileo.open.util.api_scan;

import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.Parameter;
import lombok.Data;
import lombok.experimental.Accessors;
import org.armada.galileo.annotation.openapi.ApiRole;

import java.util.List;

/**
 * @author xiaobo
 * @date 2022/12/21 18:37
 */
@Data
@Accessors(chain = true)
public class JavaFileItem {

    private String javaFileName;

    private String filePath;

    private String fileDesc;

    private String fileContent;

    private String group;

    private int groupIndex;

    private List<String> imports;

    private List<JavaMethodItem> JavaMethodItems;

    private Boolean scanDocs = false;

    /**
     * 调用方
     */
    private String apiFrom;

    /**
     * 被调用方
     */
    private String apiTo;

    private String javaPakcage;

    private String javaClassName;

    @Data
    @Accessors(chain = true)
    public static class JavaMethodItem {

        private String apiGroup;

        private int apiIndex;

        // private String apiName;

        private String methodName;

        private String methodComment;

        private String returnType;

        private Boolean applicationJsonRequest = false;

        NodeList<Parameter> parameters;

        private Boolean deprecated;

        private Boolean async = false;

    }
}
