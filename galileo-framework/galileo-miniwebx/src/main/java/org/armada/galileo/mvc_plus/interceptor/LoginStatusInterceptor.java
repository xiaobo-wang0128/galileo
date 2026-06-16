//package org.armada.galileo.mvc_plus.interceptor;
//
//import lombok.extern.slf4j.Slf4j;
//import org.armada.galileo.annotation.mvc.NoToken;
//import org.armada.galileo.common.util.JsonUtil;
//import org.armada.galileo.exception.BizException;
//import org.armada.galileo.exception.LoginTimeoutException;
//import org.armada.galileo.model.domain.LoginUser;
//import org.armada.galileo.model.domain.ThreadUser;
//import org.armada.galileo.mvc_plus.encrypt.EncryptUtil;
//import org.armada.galileo.mvc_plus.support.MiniWebxServlet;
//import org.armada.galileo.mvc_plus.util.GlobalConstant;
//import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
//
//import javax.servlet.http.Cookie;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import java.lang.reflect.Method;
//
///**
// * @author xiaobo
// * @date 2022/12/13 10:49
// */
//
//@Slf4j
//public class LoginStatusInterceptor extends HandlerInterceptorAdapter {
//
//
//    private boolean checkByCookie;
//
//    public LoginStatusInterceptor(boolean checkByCookie) {
//        this.checkByCookie = checkByCookie;
//    }
//
//    public LoginStatusInterceptor() {
//        this.checkByCookie = false;
//    }
//
//    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
//
//        Method method = (Method) request.getAttribute(MiniWebxServlet.SPRING_CONTROLLER_INVOKE_METHOD);
//        if (method == null) {
//            return true;
//        }
//
//        String cookieValue = null;
//        // 从cookie中取值
//        if (checkByCookie) {
//            Cookie[] cookies = request.getCookies();
//            if (cookies != null && cookies.length > 0) {
//                for (Cookie cookie : cookies) {
//                    if (cookie.getName().equals(GlobalConstant.Agent_User_Login_Cookie_Head)) {
//                        cookieValue = cookie.getValue();
//                        break;
//                    }
//                }
//            }
//        }
//        // from gateway
//        else {
//            cookieValue = request.getHeader(GlobalConstant.Target_User_Cookie_Head);
//        }
//
//        LoginUser u = null;
//
//        if (cookieValue != null) {
//            try {
//                String loginUserJson = EncryptUtil.aesDecodeBase64(cookieValue, GlobalConstant.KEY);
//                u = JsonUtil.fromJson(loginUserJson, LoginUser.class);
//            } catch (Exception e) {
//                throw new LoginTimeoutException();
//            }
//        }
//
//        boolean noTokenUrl = method.isAnnotationPresent(NoToken.class) || method.getDeclaringClass().isAnnotationPresent(NoToken.class);
//
//        // 用户为空， 无需登陆的接口
//        if (u == null) {
//            if (noTokenUrl) {
//                return true;
//            } else {
//                throw new LoginTimeoutException();
//            }
//        }
//        // 用户不为空
//        else {
//
//            // 必须要登陆的接口
//            if (!noTokenUrl) {
//
//                // 平台管理员
//                if (u.isSuperAdmin()) {
//                    if (method.isAnnotationPresent(CustomerLogin.class)) {
//                        throw new LoginTimeoutException();
//                    }
//                } else {
//
//                    // 无法获取用户类型
//                    if (u.getUserType() == null) {
//                        throw new LoginTimeoutException();
//                    }
//
//                    String uri = request.getRequestURI();
//
//                    // 平台管理员、机构用户
//                    if (u.isSuperAdmin() || u.isCompany()) {
//
//                        if (method.isAnnotationPresent(CustomerLogin.class)) {
//                            throw new LoginTimeoutException();
//                        }
//
//                        if (u.getUserId() == null) {
//                            throw new BizException("用户当前id为空");
//                        }
//
//                        if (u.getTenantId() == null) {
//                            throw new BizException("用户当前 tenantId 为空");
//                        }
//
//                    }
//
//                    // 客户登陆
//                    else if (u.isCustomer()) {
//
//                        // customer 型用户只能访问 CustomerLogin 标记的接口
//                        if (!method.isAnnotationPresent(CustomerLogin.class) && !method.isAnnotationPresent(NoToken.class)) {
//                            throw new LoginTimeoutException();
//                        }
//
//                        // 关键性参数为空，报错
//                        if (u.getUserId() == null || u.getTenantId() == null) {
//                            throw new BizException("用户关键性数据异常");
//                        }
//
//                    }
//                    // 其他情况不允许访问
//                    else {
//                        throw new LoginTimeoutException();
//                    }
//                }
//
//            }
//        }
//
//        ThreadUser.set(u);
//
//        return true;
//    }
//
//    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
//        ThreadUser.remove();
//    }
//
//
//}
