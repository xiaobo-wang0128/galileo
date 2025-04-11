//package org.armada.galileo.mvc_plus.interceptor;
//
//import java.lang.reflect.Method;
////import com.haigui.anotation.RefreshCache;
//
//import org.armada.galileo.mvc_plus.annotation.NoToken;
//import org.armada.galileo.exception.LoginTimeoutException;
//
//import org.armada.galileo.common.loader.SpringMvcUtil;
//import org.armada.galileo.common.threadlocal.ThreadRequest;
//import org.springframework.web.servlet.HandlerInterceptor;
//
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//
//public class MvcCommonInterceptor implements HandlerInterceptor {
//
//	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
//
//		Method method = SpringMvcUtil.getHandlerMethod(request);
//
//		LoginUser u = LoginCookieUtil.checkLoginStatus(request);
//		if (u != null) {
//			ThreadUser.set(u);
//		}
//
//		// 登陆处理
//		if (method != null && !(method.isAnnotationPresent(NoToken.class)) && !(method.getDeclaringClass().isAnnotationPresent(NoToken.class))) {
//			if (u == null) {
//				throw new LoginTimeoutException();
//			}
//		}
//
//		ThreadRequest.set(request);
//		return true;
//	}
//
//	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
//
//		ThreadUser.remove();
//		ThreadRequest.remove();
//
//	}
//
//}