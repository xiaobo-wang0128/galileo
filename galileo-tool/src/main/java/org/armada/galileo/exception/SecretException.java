package org.armada.galileo.exception;


public class SecretException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public SecretException() {
		super();
	}

	public SecretException(String msg) {
		super(msg);
	}

	public SecretException(RuntimeException e) {
		super(e);
	}

	public SecretException(Throwable e) {
		super(e);
	}
}
