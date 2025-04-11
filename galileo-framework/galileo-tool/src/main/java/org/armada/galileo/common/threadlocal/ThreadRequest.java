package org.armada.galileo.common.threadlocal;

import javax.servlet.http.HttpServletRequest;

public class ThreadRequest {

	private static ThreadLocal<HttpServletRequest> local = new ThreadLocal<HttpServletRequest>();

	public static void set(HttpServletRequest request) {
		local.set(request);
	}

	public static HttpServletRequest get() {
		return local.get();
	}

	public static void remove() {
		local.remove();
	}

}
