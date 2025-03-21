package org.armada.galileo.open.util.api_scan.domain;


import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

@Data
@Accessors(chain = true)
public class ParamType implements Serializable {

    private static final long serialVersionUID = 1L;

    // 参数名称
    private String name;

    // 类型
    private String type;

    // 长度
    private Integer length = -1;

    // 描述信息
    private String description;

    // 子参数列表
    private List<ParamType> subParams;

    // 系统框架参数，如 userId token appId
    // private Boolean isSystem = false;

    // 必填项
    private Boolean notNull = false;

    /**
     * 最大值
     */
    private Integer min = -1;

    /**
     * 最小值
     */
    private Integer max = -1;


    private Boolean noDoc;

    public ParamType() {

    }

    public ParamType(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public ParamType(String name, String type, String description, Boolean isSystem) {
        this.name = name;
        this.type = type;
        this.description = description;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("ParamType{");
        sb.append("name='").append(name).append('\'');
        sb.append(", type='").append(type).append('\'');
        sb.append(", length=").append(length);
        sb.append(", description='").append(description).append('\'');
        sb.append(", subParams=").append(subParams);
        sb.append(", notNull=").append(notNull);
        sb.append(", min=").append(min);
        sb.append(", max=").append(max);
        sb.append('}');
        return sb.toString();
    }


}
