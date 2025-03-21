package org.armada.galileo.open.constant;

/**
 * @author xiaobo
 * @date 2023/2/8 10:32
 */
public enum RequestMessageStatus {

    DOING("通信中"),

    SUCCESS("成功"),

    FAIL("通信失败"),

    ;

    private String desc;

    RequestMessageStatus(String desc) {
        this.desc = desc;
    }


    public String getDesc() {
        return this.desc;
    }
}