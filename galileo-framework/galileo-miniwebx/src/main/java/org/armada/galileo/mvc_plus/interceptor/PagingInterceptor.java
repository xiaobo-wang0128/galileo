package org.armada.galileo.mvc_plus.interceptor;


import org.armada.galileo.common.page.PageParam;
import org.armada.galileo.common.page.ThreadPagingUtil;
import org.armada.galileo.common.util.CommonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


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

        String _target_page = request.getParameter("pageIndex");
        String _page_size = request.getParameter("pageSize");
        String _order_by = request.getParameter("orderBy");

        if (_target_page != null && _page_size != null && !_target_page.equals("") && !_page_size.equals("")) {
            try {
                PageParam pageParam = PageParam.instanceByPageIndex(Integer.valueOf(_target_page),  Integer.valueOf(_page_size));

                if (_order_by != null && !_order_by.matches("\\s*")) {
                    _order_by = _order_by.trim();
                    if (_order_by.charAt(0) == '-' && _order_by.length() >= 2) {
                        _order_by = _order_by.substring(1);
                        String orderby = CommonUtil.convertJavaField2DB(_order_by);
                        pageParam.setOrderByColumn(orderby);
                        pageParam.setOrderByType(DESC);
                    }
                    else {
                        String orderby = CommonUtil.convertJavaField2DB(_order_by);
                        pageParam.setOrderByColumn(orderby);
                        pageParam.setOrderByType(ASC);
                    }
                }

                ThreadPagingUtil.set(pageParam);

            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
        return true;
    }

    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        ThreadPagingUtil.clear();
    }

}
