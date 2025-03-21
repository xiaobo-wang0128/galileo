package org.armada.galileo.user.util;

import javax.security.auth.login.LoginException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;
import org.armada.galileo.common.util.JsonUtil;
import org.armada.galileo.exception.LoginTimeoutException;
import org.armada.galileo.mvc_plus.encrypt.EncryptUtil;
import org.armada.galileo.exception.BizException;
import org.armada.galileo.user.domain.LoginUser;

@Slf4j
public class LoginCookieUtil {

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

    public static void setLoginData(LoginUser u, HttpServletRequest request, HttpServletResponse response) {
        if (u == null) {
            throw new BizException("user is null");
        }
        try {
            String cookieValue = JsonUtil.toJson(u);
            String encode = EncryptUtil.aesEncode(cookieValue, KEY);

            Cookie cookie = new Cookie(GlobalCookieKey, encode);
            cookie.setMaxAge(24 * 60 * 60);
            cookie.setPath("/");
            cookie.setHttpOnly(true);
            // cookie.setDomain("");

            response.addCookie(cookie);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    public static void clearLogin(HttpServletRequest request, HttpServletResponse response) {

        String cookieValue = "";
        Cookie cookie = new Cookie(GlobalCookieKey, cookieValue);

        cookie.setMaxAge(0);

        cookie.setPath("/");
        cookie.setHttpOnly(true);

        response.addCookie(cookie);
    }

    public static LoginUser checkLoginStatus(HttpServletRequest request) {

        Cookie[] cookies = request.getCookies();
        if (cookies == null || cookies.length == 0) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(GlobalCookieKey)) {
                String v = cookie.getValue();
                if (v != null) {
                    try {
                        String decode = EncryptUtil.aesDecode(v, KEY);

                        LoginUser u = JsonUtil.fromJson(decode, LoginUser.class);
                        if (u == null) {
                            throw new LoginException();
                        }
                        return u;

                    } catch (Exception e) {
                        log.error(e.getMessage());
                        throw new LoginTimeoutException();
                    }
                }
            }
        }
        return null;
    }

    /**
     * 将二进制转换成16进制字符串
     *
     * @param buf
     * @return
     */
    public static String byte2Hex(byte buf[]) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < buf.length; i++) {
            String hex = Integer.toHexString(buf[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            sb.append(hex.toUpperCase());
        }
        return sb.toString();
    }

    /**
     * java将16进制字符串转换为二进制数组
     *
     * @param hexStr
     * @return
     */
    public static byte[] hex2Byte(String hexStr) {
        if (hexStr.length() < 1)
            return null;
        byte[] result = new byte[hexStr.length() / 2];
        for (int i = 0; i < hexStr.length() / 2; i++) {
            int high = Integer.parseInt(hexStr.substring(i * 2, i * 2 + 1), 16);
            int low = Integer.parseInt(hexStr.substring(i * 2 + 1, i * 2 + 2), 16);
            result[i] = (byte) (high * 16 + low);
        }
        return result;
    }

}
