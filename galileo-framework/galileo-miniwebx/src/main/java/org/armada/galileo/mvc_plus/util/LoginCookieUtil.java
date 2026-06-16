package org.armada.galileo.mvc_plus.util;

import lombok.extern.slf4j.Slf4j;
import org.armada.galileo.common.util.JsonUtil;
import org.armada.galileo.exception.BizException;
import org.armada.galileo.model.domain.LoginUser;
import org.armada.galileo.model.domain.ThreadUser;
import org.armada.galileo.mvc_plus.encrypt.EncryptUtil;
import org.springframework.http.HttpHeaders;
import org.springframework.util.StringUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
public class LoginCookieUtil {

//    public static void main(String[] args) {
//        String encode = EncryptUtil.aesEncodeBase64("cookieValue", KEY);
//        String dec = EncryptUtil.aesDecodeBase64("zt65s4Avx0W7dnLZVToqQQ==" , KEY);
//        System.out.println(encode);
//        System.out.println(dec);
//    }

    public static String Agent_User_Login_Cookie_Head = "";

    public static String Open_User_Login_Cookie_Head = "";

    public static String KEY = "";

    public static void setCookie(String cookieKey, String cookieValue, HttpServletResponse response) {
        if (Agent_User_Login_Cookie_Head.equals(cookieKey)) {
            throw new BizException("该 cookie不能使用");
        }
        try {
            String encode = EncryptUtil.aesEncodeBase64(cookieValue, KEY);
            Cookie cookie = new Cookie(cookieKey, encode);
            cookie.setMaxAge(24 * 60 * 60);
            cookie.setPath("/");
            cookie.setHttpOnly(true);
            response.addCookie(cookie);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }


    public static void setCookieWithoutEncrypt(String cookieKey, String cookieValue, HttpServletResponse response) {
        if (Agent_User_Login_Cookie_Head.equals(cookieKey)) {
            throw new BizException("该 cookie不能使用");
        }
        try {
            Cookie cookie = new Cookie(cookieKey, cookieValue);
            cookie.setMaxAge(24 * 180 * 60 * 60);
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
            return null;
        }
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(cookieKey)) {
                String v = cookie.getValue();
                try {
                    v = EncryptUtil.aesDecodeBase64(v, KEY);
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


    public static void clearLogin(HttpServletResponse response) {
        clearCookie(Agent_User_Login_Cookie_Head, response);
        clearCookie(Open_User_Login_Cookie_Head, response);
    }


    public static void setLoginData(String cookieKey, LoginUser u, HttpServletResponse response) {
        if (u == null) {
            throw new BizException("user is null");
        }
        try {
            String cookieValue = JsonUtil.toJson(u);
            String encode = EncryptUtil.aesEncodeBase64(cookieValue, KEY);
            Cookie cookie = new Cookie(cookieKey, encode);
            cookie.setMaxAge(24 * 60 * 60);
            cookie.setPath("/");
            cookie.setHttpOnly(true);

            response.addCookie(cookie);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }


    public static void main(String[] args) {
        String encode = EncryptUtil.aesEncodeBase64("{abd}", KEY);
        Cookie cookie = new Cookie(Agent_User_Login_Cookie_Head, encode);
        cookie.setMaxAge(24 * 60 * 60);
        cookie.setPath("/");
        cookie.setHttpOnly(true);

        System.out.println(getCookieHeader(cookie));
    }

    public static String getCookieHeader(Cookie cookie) {
        StringBuilder buf = new StringBuilder();
        buf.append(cookie.getName()).append('=').append(cookie.getValue() == null ? "" : cookie.getValue());
        if (StringUtils.hasText(cookie.getPath())) {
            buf.append("; Path=").append(cookie.getPath());
        }

        if (StringUtils.hasText(cookie.getDomain())) {
            buf.append("; Domain=").append(cookie.getDomain());
        }

        int maxAge = cookie.getMaxAge();
        ZonedDateTime expires = null;
        if (maxAge >= 0) {
            buf.append("; Max-Age=").append(maxAge);
            buf.append("; Expires=");
            if (expires != null) {
                buf.append(expires.format(DateTimeFormatter.RFC_1123_DATE_TIME));
            } else {
                HttpHeaders headers = new HttpHeaders();
                headers.setExpires(maxAge > 0 ? System.currentTimeMillis() + 1000L * (long) maxAge : 0L);
                buf.append(headers.getFirst("Expires"));
            }
        } else if (expires != null) {
            buf.append("; Expires=");
            buf.append(expires.format(DateTimeFormatter.RFC_1123_DATE_TIME));
        }

        if (cookie.getSecure()) {
            buf.append("; Secure");
        }

        if (cookie.isHttpOnly()) {
            buf.append("; HttpOnly");
        }

        return buf.toString();
    }

    public static String setLoginData(LoginUser u, HttpServletResponse response) {
        if (u == null) {
            throw new BizException("user is null");
        }
        try {
            String cookieValue = JsonUtil.toJson(u);
            String encode = EncryptUtil.aesEncodeBase64(cookieValue, KEY);
            Cookie cookie = new Cookie(Agent_User_Login_Cookie_Head, encode);
            cookie.setMaxAge(24 * 60 * 60);
            cookie.setPath("/");
            cookie.setHttpOnly(true);

            if (response != null) {
                response.addCookie(cookie);
            }

            return encode;

        } catch (Exception e) {
            log.error(e.getMessage(), e);
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
        if (hexStr.length() < 1) return null;
        byte[] result = new byte[hexStr.length() / 2];
        for (int i = 0; i < hexStr.length() / 2; i++) {
            int high = Integer.parseInt(hexStr.substring(i * 2, i * 2 + 1), 16);
            int low = Integer.parseInt(hexStr.substring(i * 2 + 1, i * 2 + 2), 16);
            result[i] = (byte) (high * 16 + low);
        }
        return result;
    }


    public static LoginUser getUserByCookie(String cookieHead, HttpServletRequest request) {
        String v = LoginCookieUtil.getCookie(cookieHead, request);
        LoginUser loginUser = null;
        if (v != null) {
            try {
                loginUser = JsonUtil.fromJson(v, LoginUser.class);
                ThreadUser.set(loginUser);
            } catch (Exception e) {
            }
        }
        return loginUser;
    }

}
