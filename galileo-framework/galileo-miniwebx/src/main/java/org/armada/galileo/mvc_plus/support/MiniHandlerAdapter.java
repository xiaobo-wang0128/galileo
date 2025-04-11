package org.armada.galileo.mvc_plus.support;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.HandlerAdapter;
import org.springframework.web.servlet.ModelAndView;

public class MiniHandlerAdapter implements HandlerAdapter {

	// this.handlerAdapters

	public boolean supports(Object handler) {

		return true;
	}

	public ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		return null;
	}

	public long getLastModified(HttpServletRequest request, Object handler) {
		return 0;
	}

}
