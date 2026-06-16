package org.armada.galileo.mvc_plus.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author xiaobo
 * @date 2022/12/13 10:49
 */

@Slf4j
public class CorsInterceptor extends HandlerInterceptorAdapter {

    public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) {
        // 设置允许的前端域名
        res.setHeader("Access-Control-Allow-Origin", "*");
        // 允许携带 Cookie
        res.setHeader("Access-Control-Allow-Credentials", "true");
        // 允许的请求方法
        res.setHeader("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS");
        // 允许的请求头
        res.setHeader("Access-Control-Allow-Headers", "Content-Type,Authorization,X-Requested-With");
        // 预检请求缓存时间
        res.setHeader("Access-Control-Max-Age", "3600");
        return true;
    }

    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
    }

}
