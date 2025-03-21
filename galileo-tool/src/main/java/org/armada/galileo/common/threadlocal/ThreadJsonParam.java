package org.armada.galileo.common.threadlocal;

public class ThreadJsonParam {

	private static ThreadLocal<String> local = new ThreadLocal<String>();

	public static void set(String request) {
		local.set(request);
	}

	public static String get() {
		return local.get();
	}

	public static void remove() {
		local.remove();
	}

}
