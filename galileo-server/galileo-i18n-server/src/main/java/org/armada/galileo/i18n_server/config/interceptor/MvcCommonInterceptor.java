package org.armada.galileo.i18n_server.config.interceptor;

import org.armada.galileo.common.threadlocal.ThreadRequest;
import org.armada.galileo.exception.LoginTimeoutException;
import org.armada.galileo.i18n_server.user.LoginUser;
import org.armada.galileo.i18n_server.user.ThreadUser;
import org.armada.galileo.i18n_server.util.LoginCookieUtil;
import org.armada.galileo.annotation.mvc.NoToken;
import org.armada.galileo.mvc_plus.support.MiniWebxServlet;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

public class MvcCommonInterceptor implements HandlerInterceptor {

    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {

//        response.setHeader("Access-Control-Allow-Origin", "*");
//        response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE");
//        response.setHeader("Access-Control-Max-Age", "1200000");
//        response.setHeader("Access-Control-Allow-Headers", "*");

        Method method = (Method) request.getAttribute(MiniWebxServlet.SPRING_CONTROLLER_INVOKE_METHOD);
        if (method == null) {
            return true;
        }
        LoginUser u = LoginCookieUtil.checkLoginStatus(request);
        if (u != null) {
            ThreadUser.set(u);
        }

        // 登陆处理
        if (method != null && !(method.isAnnotationPresent(NoToken.class)) && !(method.getDeclaringClass().isAnnotationPresent(NoToken.class))) {
            if (u == null) {
                throw new LoginTimeoutException();
            }
        }

        ThreadRequest.set(request);
        return true;
    }

    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {

        ThreadUser.remove();
        ThreadRequest.remove();

    }

}