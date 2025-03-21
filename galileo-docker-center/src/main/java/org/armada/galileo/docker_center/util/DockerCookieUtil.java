package org.armada.galileo.docker_center.util;

import com.google.common.reflect.TypeToken;
import org.armada.galileo.common.util.CommonUtil;
import org.armada.galileo.common.util.JsonUtil;
import org.armada.galileo.common.util.ObjectUtil;
import org.armada.galileo.docker_center.vo.ImageRequestVO;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * @author xiaobo
 * @date 2022/3/12 4:58 PM
 */
public class DockerCookieUtil {

    private static String CookieHead = "_haiq_docker_";

    public static List<ImageRequestVO> getLocalRequests(HttpServletRequest request) {
        String cookie = null;
        try {
            cookie = CookieUtil.getCookie(CookieHead, request);
        } catch (Exception e) {
        }
        if (CommonUtil.isEmpty(cookie)) {
            return null;
        }
        try {
            byte[] bufs = CommonUtil.base64Decode(cookie);
            List<ImageRequestVO> list = JsonUtil.fromJson(new String(bufs, StandardCharsets.UTF_8), new TypeToken<List<ImageRequestVO>>() {
            }.getType());
            return list;
        } catch (Exception e) {
        }
        return new ArrayList<>();
    }

    public static void updateToCookie(List<ImageRequestVO> voList, HttpServletResponse response) {
        if (CommonUtil.isEmpty(voList)) {
            return;
        }
        byte[] bufs = JsonUtil.toJson(voList).getBytes(StandardCharsets.UTF_8);
        String v = CommonUtil.base64Encode(bufs);
        CookieUtil.setCookie(CookieHead, v, response);
    }

    public static void clearCookie(HttpServletResponse response) {
        CookieUtil.clearCookie(CookieHead, response);
    }
}
