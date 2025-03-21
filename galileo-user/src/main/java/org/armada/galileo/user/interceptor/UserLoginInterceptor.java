package org.armada.galileo.user.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.armada.galileo.exception.LoginTimeoutException;
import org.armada.galileo.annotation.mvc.NoToken;
import org.armada.galileo.mvc_plus.support.MiniWebxServlet;
import org.armada.galileo.user.domain.LoginUser;
import org.armada.galileo.user.domain.ThreadUser;
import org.armada.galileo.user.util.LoginCookieUtil;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

@Slf4j
public class UserLoginInterceptor implements HandlerInterceptor {

    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {

        Method method = null;

        // spring-mvc 原始拦截器
        if (handler instanceof HandlerMethod) {
            HandlerMethod h = (HandlerMethod) handler;
            method = h.getMethod();

        }
        // miniwebx 拦截器
        else {
            method = (Method) request.getAttribute(MiniWebxServlet.SPRING_CONTROLLER_INVOKE_METHOD);
        }

        LoginUser u = LoginCookieUtil.checkLoginStatus(request);
        if (u != null) {
            ThreadUser.set(u);
            return true;
        }

        // 登陆处理
        if (method != null && !(method.isAnnotationPresent(NoToken.class)) && !(method.getDeclaringClass().isAnnotationPresent(NoToken.class))) {
            throw new LoginTimeoutException();
        }

        return true;
    }

    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        ThreadUser.remove();
    }

}