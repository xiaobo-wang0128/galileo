package org.armada.galileo.exception;

import java.util.Map;

import org.armada.galileo.common.util.CommonUtil;
import org.armada.galileo.i18n.I18nUtil;
import org.slf4j.helpers.MessageFormatter;

public class BizException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private Map<String, Object> errorMap;

    private String errorMsg;

    private I18nError error;

    public BizException() {
    }

    public BizException(I18nError error) {
        this(error, null);
    }

    public BizException(I18nError error, String... args) {

        String errorMsg = "";
        if (error != null) {
            String desc = error.getDesc();
            String clsName = error.getClass().getName();
            clsName = clsName.substring(clsName.lastIndexOf(".") + 1);
            String i18nKey = clsName + "__" + error.toString();
            errorMsg = I18nUtil.get(i18nKey, desc);
            if (args != null && args.length > 0) {
                for (int i = 0; i < args.length; i++) {
                    if (args[i] == null) {
                        args[i] = "";
                    }
                }
                errorMsg = MessageFormatter.arrayFormat(errorMsg, args).getMessage();
            }
            errorMsg = CommonUtil.replaceAll(errorMsg, "{}", "");
        }

        this.errorMsg = errorMsg;
        this.error = error;

    }

    //@Deprecated
    public BizException(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    //@Deprecated
    public BizException(String format, String... argArray) {
        this.errorMsg = MessageFormatter.arrayFormat(format, argArray).getMessage();
    }

    //@Deprecated
    public BizException(Object errorMsg) {
        this.errorMsg = errorMsg != null ? errorMsg.toString() : "";
    }

    public BizException(String errorMsg, Map<String, Object> errorMap) {
        super(errorMsg);
        this.errorMsg = errorMsg;
        this.errorMap = errorMap;
    }

    public BizException(RuntimeException e) {
        super(e);
    }

    public BizException(Throwable e) {
        super(e);
    }

    public Map<String, Object> getErrorMap() {
        return errorMap;
    }

    @Override
    public String getMessage() {
        return errorMsg;
    }

    public I18nError getError() {
        return error;
    }

    @Override
    public String toString() {
        return errorMsg;
    }
}
