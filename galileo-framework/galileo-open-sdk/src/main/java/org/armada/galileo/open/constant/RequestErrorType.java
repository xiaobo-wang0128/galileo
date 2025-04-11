package org.armada.galileo.open.constant;

/**
 * @author xiaobo
 * @date 2023/2/8 10:32
 */
public enum RequestErrorType {

    IO("io异常"),

    BIZ("业务异常"),


    ;

    private String desc;

    RequestErrorType(String desc) {
        this.desc = desc;
    }


    public String getDesc() {
        return this.desc;
    }
}