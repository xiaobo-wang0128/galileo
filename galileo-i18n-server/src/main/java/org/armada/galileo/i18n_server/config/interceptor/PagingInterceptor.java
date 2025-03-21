package org.armada.galileo.i18n_server.config.interceptor;

import cn.hutool.core.util.StrUtil;
import org.armada.galileo.common.page.PageParam;
import org.armada.galileo.common.page.ThreadPagingUtil;
import org.armada.galileo.i18n_server.config.mvc.WriteAble;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

/**
 * 权限处理拦截器
 *
 * @author xiaobowang 2018年9月5日
 */
public class PagingInterceptor extends HandlerInterceptorAdapter {

    private static Logger log = LoggerFactory.getLogger(PagingInterceptor.class);

    private static final String ASC = "asc";

    private static final String DESC = "desc";

    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {

        String pageIndex = request.getParameter("pageIndex");
        String pageSize = request.getParameter("pageSize");

        if (StrUtil.isNotEmpty(pageIndex) || StrUtil.isNotEmpty(pageSize)) {
            try {
                PageParam pageParam = PageParam.instanceByPageIndex(1, 10);

                if (StrUtil.isNotEmpty(pageIndex)) {
                    pageParam.setPageIndex(Integer.valueOf(pageIndex));
                }
                if (StrUtil.isNotEmpty(pageSize)) {
                    pageParam.setPageSize(Integer.valueOf(pageSize));
                }
                ThreadPagingUtil.set(pageParam);
            } catch (Exception e) {
                log.error("请求地址:{},出现分页异常。", request.getRequestURI());
                log.error(e.getMessage(), e);
            }
        }
        return true;
    }


    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception
            ex) {
        ThreadPagingUtil.clear();
    }

}
