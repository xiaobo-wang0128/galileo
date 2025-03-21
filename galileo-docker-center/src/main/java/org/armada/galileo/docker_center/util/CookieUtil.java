package org.armada.galileo.docker_center.util;

import lombok.extern.slf4j.Slf4j;
import org.armada.galileo.exception.BizException;
import org.armada.galileo.exception.LoginTimeoutException;
import org.armada.galileo.mvc_plus.encrypt.EncryptUtil;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
public class CookieUtil {

    private static String GlobalCookieKey = "_haiq_";

    private static String KEY = "paipkb6763n2150o";

    public static void setCookie(String cookieKey, String cookieValue, HttpServletResponse response) {

        if (GlobalCookieKey.equals(cookieKey)) {
            throw new BizException("该 cookie不能使用");
        }

        try {
            String encode = EncryptUtil.aesEncode(cookieValue, KEY);

            Cookie cookie = new Cookie(cookieKey, encode);
            cookie.setMaxAge(24 * 60 * 60);
            cookie.setPath("/");
            cookie.setHttpOnly(true);

            response.addCookie(cookie);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    public static String getCookie(String cookieKey, HttpServletRequest request) {

        Cookie[] cookies = request.getCookies();
        if (cookies == null || cookies.length == 0) {
            throw new LoginTimeoutException();
        }
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(cookieKey)) {
                String v = cookie.getValue();
                try {
                    v = EncryptUtil.aesDecode(v, KEY);
                } catch (Exception e) {
                }
                return v;
            }
        }
        return null;
    }

    public static void clearCookie(String cookieKey, HttpServletResponse response) {
        String cookieValue = "";
        Cookie cookie = new Cookie(cookieKey, cookieValue);

        cookie.setMaxAge(0);

        cookie.setPath("/");
        cookie.setHttpOnly(true);

        response.addCookie(cookie);
    }

}
