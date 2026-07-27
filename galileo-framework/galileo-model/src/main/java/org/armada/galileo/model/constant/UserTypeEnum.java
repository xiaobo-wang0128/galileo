package org.armada.galileo.model.constant;

/**
 * @author xiaobo
 *
 * @date 2023/4/11 10:57
 */
public enum UserTypeEnum implements I18nDictionary {

    COMPANY("机构用户"),

    CUSTOMER("客户"),

    PLATFORM("第三方平台用户"),
    ;

    private String desc;

    UserTypeEnum(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return this.desc;
    }

}
