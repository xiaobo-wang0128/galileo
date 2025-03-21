package org.armada.spi.open.util;


/**
 * SDK 远程调用异常信息
 *
 * @author xiaobo
 * @date 2021/4/23 10:07 上午
 */
public class HaiqApiException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * 错误码
     */
    private String errorCode;

    /**
     * 错误信息
     */
    private String errorMsg;

    public HaiqApiException() {
        super();
    }

    public HaiqApiException(String errorMsg) {
        super(errorMsg);
        this.errorMsg = errorMsg;
    }

    public HaiqApiException(RuntimeException e) {
        super(e);
    }

    public HaiqApiException(Throwable e) {
        super(e);
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorMsg() {
        return errorMsg;
    }
}
