package org.armada.galileo.mvc_plus.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.armada.galileo.common.util.CommonUtil;
import org.armada.galileo.i18n.I18nContext;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author xiaobo
 * @date 2022/12/13 10:49
 */

@Slf4j
public class I18nSpringInterceptor extends HandlerInterceptorAdapter {

    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {


        String lan = request.getHeader("Content-Language");
        // 自定义 头
        if (CommonUtil.isNotEmpty(lan)) {
            I18nContext.setI18nContext(lan);
        }
        // 浏览器默认头
        else {
            String tmps = request.getHeader("Accept-Language");
            if (tmps == null) {
                return true;
            }

            lan = tmps.split(";")[0].trim();

            if (lan.indexOf("-") != -1) {
                lan = lan.substring(0, lan.indexOf("-"));
            }
            I18nContext.setI18nContext(lan);
        }

        return true;
    }

    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        I18nContext.remove();
    }


}
