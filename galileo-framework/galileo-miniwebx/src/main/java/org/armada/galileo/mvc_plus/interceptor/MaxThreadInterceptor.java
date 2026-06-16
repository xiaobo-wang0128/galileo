package org.armada.galileo.mvc_plus.interceptor;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;


import org.armada.galileo.annotation.mvc.MaxThread;
import org.armada.galileo.exception.BizException;
import org.armada.galileo.mvc_plus.support.MiniWebxServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.armada.galileo.common.loader.SpringMvcUtil;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * 权限处理拦截器
 *
 * @author xiaobowang 2018年9月5日
 */
public class MaxThreadInterceptor implements HandlerInterceptor {

    private static Logger log = LoggerFactory.getLogger(MaxThreadInterceptor.class);

    private static ConcurrentHashMap<String, AtomicInteger> uriMap = new ConcurrentHashMap<>();

    ThreadLocal<Boolean> local = new ThreadLocal<>();

    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {

        Method method = (Method) request.getAttribute(MiniWebxServlet.SPRING_CONTROLLER_INVOKE_METHOD);
        if (method == null) {
            return true;
        }

        String uri = request.getRequestURI();

        // 并发限流控制
        if (method != null && method.isAnnotationPresent(MaxThread.class)) {
            local.set(true);

            MaxThread mt = method.getAnnotation(MaxThread.class);
            int max = mt.value();
            if (max <= 0) {
                return true;
            }

            AtomicInteger times = uriMap.get(uri);

            if (times == null) {
                times = new AtomicInteger(0);
            }

            times.incrementAndGet();

            uriMap.put(uri, times);

            log.info("pre: " + times.get());
            if (times.get() > max) {
                throw new BizException("系统繁忙，请稍后再试");
            }
        }

        return true;
    }

    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {

        if (local.get() != null && local.get()) {
            String uri = request.getRequestURI();
            AtomicInteger times = uriMap.get(uri);

            if (times != null) {
                times.decrementAndGet();

                log.info("after: " + times.get());

                uriMap.put(uri, times);
            }
        }

        local.remove();
    }

}
