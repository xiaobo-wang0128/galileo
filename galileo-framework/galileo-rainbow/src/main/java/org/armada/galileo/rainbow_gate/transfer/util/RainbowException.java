package org.armada.galileo.rainbow_gate.transfer.util;

public class RainbowException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public RainbowException() {
        super();
    }

    public RainbowException(String errorMsg) {
        super(errorMsg);
    }
 
    
    public RainbowException(Exception e) {
        super(e);
    }
 
}
