package org.armada.galileo.auto_code.util.redis;

import java.util.Map;

import org.slf4j.helpers.MessageFormatter;

/**
 * @author xiaobo
 * @date 2022/12/7 16:42
 */
public class JedisLockException extends RuntimeException{

    private static final long serialVersionUID = 1L;

    private Object errorData;

    public JedisLockException() {
        super();
    }

    public JedisLockException(String errorMsg) {
        super(errorMsg);
    }

    public JedisLockException(String format, String... argArray) {
        super(MessageFormatter.arrayFormat(format, argArray).getMessage());
    }

    public JedisLockException(Object errorMsg) {
        super(errorMsg != null ? errorMsg.toString() : "");
    }

    public JedisLockException(String errorMsg, Map<String, Object> errorMap) {
        super(errorMsg);
        this.errorData = errorMap;
    }

    public JedisLockException(RuntimeException e) {
        super(e);
    }

    public JedisLockException(Throwable e) {
        super(e);
    }

    public Object getErrorData() {
        return errorData;
    }
}
