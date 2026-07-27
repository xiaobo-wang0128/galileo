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

    public static YesOrNoEnum getByDesc(String desc){
        if(desc == null){
            return null;
        }
        for (YesOrNoEnum belongEnum:YesOrNoEnum.values()) {
            if(belongEnum.getDesc().equals(desc)){
                return belongEnum;
            }
        }
        return null;
    }

    /**
     * Excel 导入兼容：Y/N、YES/NO、是/否（忽略大小写与首尾空格）
     */
    public static YesOrNoEnum parse(String value) {
        if (value == null) {
            return null;
        }
        String v = value.trim();
        if (v.isEmpty()) {
            return null;
        }
        if ("Y".equalsIgnoreCase(v) || "YES".equalsIgnoreCase(v) || "是".equals(v)) {
            return Y;
        }
        if ("N".equalsIgnoreCase(v) || "NO".equalsIgnoreCase(v) || "否".equals(v)) {
            return N;
        }
        return getByDesc(v);
    }
}
