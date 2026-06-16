package org.armada.galileo.mvc_plus.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.armada.galileo.common.util.CommonUtil;
import org.armada.galileo.mvc_plus.util.TraceIdUtil;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author xiaobo
 * @date 2022/12/13 10:49
 */

@Slf4j
public class TraceIdInterceptor extends HandlerInterceptorAdapter {

    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        TraceIdUtil.putTraceId(CommonUtil.uuid());
        return true;
    }

    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        TraceIdUtil.remove();
    }

}
