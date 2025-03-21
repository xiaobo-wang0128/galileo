package org.armada.galileo.nova_flow.exception;

import java.io.Serializable;
import java.util.Map;

import org.slf4j.helpers.MessageFormatter;

public class FlowException extends RuntimeException implements Serializable {

    private static final long serialVersionUID = 1L;

    private Object errorData;

    public FlowException() {
        super();
    }

    public FlowException(String errorMsg) {
        super(errorMsg);
    }

    public FlowException(String format, String... argArray) {
        super(MessageFormatter.arrayFormat(format, argArray).getMessage());
    }

    public FlowException(Object errorMsg) {
        super(errorMsg != null ? errorMsg.toString() : "");
    }

    public FlowException(String errorMsg, Map<String, Object> errorMap) {
        super(errorMsg);
        this.errorData = errorMap;
    }

    public FlowException(FlowException e) {
        this(e.getMessage());
    }

    public FlowException(RuntimeException e) {
        super(e);
    }

    public FlowException(Throwable e) {
        super(e);
    }

    public Object getErrorData() {
        return errorData;
    }

}
