package org.armada.galileo.open.util.api_scan.domain;


import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

@Data
@Accessors(chain = true)
public class DocItem implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 接口code
     */
    private String apiCode;

    /**
     * 接口类名
     */
    private String apiClassName;

    /**
     * 接口名
     */
    private String apiName;

    /**
     * 接口详情描述
     */
    private String apiDesc;

    /**
     * 接口地址
     */
    private String apiUrl;

    /**
     * 是否分页
     */
    private Boolean isPage = false;

    /**
     * 参数列表
     */
    private List<ParamType> inputParams;

    /**
     * 返回对象参数属性(returnObjectType为 object时有效)
     */
    private List<ParamType> outputParams;

    /**
     * 返回对象类型(string float void...)
     */
    private String outputType;

    /**
     * 返回值说明
     */
    private String outputDesc;

    /**
     * 所属分组
     */
    private String group;

    /**
     * 调用方
     */
    private String apiFrom;

    /**
     * 被调用方
     */
    private String apiTo;

    /**
     * 分组的排序
     */
    private Integer sort = 0;

    /**
     * 子排序
     */
    private Integer subSort = 0;

    /**
     * 是否废弃
     */
    private Boolean isDeprecated = false;

    /**
     * 异步调用
     */
    private Boolean async;

    /**
     * 输入参数 mock
     */
    private String inputMock;

    /**
     * 输出参数 mock
     */
    private String outputMock;

    /**
     * 入参是否为 application/json 形式 ( false 代码以 from 表单形式）
     */
    private Boolean inputIsJson;

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("DocItem{");
        sb.append("apiCode='").append(apiCode).append('\'');
        sb.append(", apiClassName='").append(apiClassName).append('\'');
        sb.append(", apiName='").append(apiName).append('\'');
        sb.append(", apiDesc='").append(apiDesc).append('\'');
        sb.append(", apiUrl='").append(apiUrl).append('\'');
        sb.append(", isPage=").append(isPage);
        sb.append(", inputParams=").append(inputParams);
        sb.append(", outputParams=").append(outputParams);
        sb.append(", outputType='").append(outputType).append('\'');
        sb.append(", outputDesc='").append(outputDesc).append('\'');
        sb.append(", group='").append(group).append('\'');
        sb.append(", apiFrom='").append(apiFrom).append('\'');
        sb.append(", apiTo='").append(apiTo).append('\'');
        sb.append(", sort=").append(sort);
        sb.append(", subSort=").append(subSort);
        sb.append(", isDeprecated=").append(isDeprecated);
        sb.append(", async=").append(async);
        sb.append(", inputMock='").append(inputMock).append('\'');
        sb.append(", outputMock='").append(outputMock).append('\'');
        sb.append(", inputIsJson=").append(inputIsJson);
        sb.append('}');
        return sb.toString();
    }
}
