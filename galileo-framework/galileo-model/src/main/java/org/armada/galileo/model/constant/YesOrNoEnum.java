package org.armada.galileo.model.constant;


/**
 * @author xiaobo
 * @date 2022/12/15 10:17
 */
public enum YesOrNoEnum implements I18nDictionary {

    Y("是"),

    N("否");

    private String desc;

    YesOrNoEnum(String desc) {
        this.desc = desc;
    }

    @Override
    public String getDesc() {
        return this.desc;
    }


    public static Boolean isY(YesOrNoEnum yesOrNoEnum) {
        return yesOrNoEnum == Y;
    }
}
