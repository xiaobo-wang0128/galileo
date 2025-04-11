package org.armada.galileo.mvc_plus.support;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.HandlerMapping;

public class MiniHandlerMapping implements HandlerMapping {

	
	//this.handlerMappings
	public HandlerExecutionChain getHandler(HttpServletRequest request) throws Exception {
		return null;
	}

}
