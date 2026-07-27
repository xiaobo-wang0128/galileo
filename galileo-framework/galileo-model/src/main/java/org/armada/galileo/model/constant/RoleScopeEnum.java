package org.armada.galileo.model.constant;

/**
 * @author xiaobo
 *
 * @date 2023/4/11 10:57
 */
public enum RoleScopeEnum {

    DEFAULT("平台管理角色"),

    COMPANY("机构用户角色"),

    ;

    private String desc;

    RoleScopeEnum(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return this.desc;
    }

}
