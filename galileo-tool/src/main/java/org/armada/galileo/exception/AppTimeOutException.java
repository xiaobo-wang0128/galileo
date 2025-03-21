package org.armada.galileo.exception;


public class AppTimeOutException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public AppTimeOutException() {
		super();
	}

	public AppTimeOutException(String msg) {
		super(msg);
	}

	public AppTimeOutException(RuntimeException e) {
		super(e);
	}

	public AppTimeOutException(Throwable e) {
		super(e);
	}
}
