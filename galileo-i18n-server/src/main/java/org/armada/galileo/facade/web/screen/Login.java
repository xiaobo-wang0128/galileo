package org.armada.galileo.facade.web.screen;

import com.google.gson.reflect.TypeToken;
import org.armada.galileo.common.util.CommonUtil;
import org.armada.galileo.common.util.Convert;
import org.armada.galileo.common.util.HttpUtil;
import org.armada.galileo.common.util.JsonUtil;
import org.armada.galileo.i18n_server.user.LoginUser;
import org.armada.galileo.i18n_server.util.LoginCookieUtil;
import org.armada.galileo.annotation.mvc.NoToken;
import org.armada.galileo.mvc_plus.encrypt.EncryptUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.Map;

/**
 * @author xiaobo
 * @date 2022/12/29 15:45
 */
@Controller
public class Login {

    private static final String key = "13d3030dd33a30";

    @Value("${galileo.sophonUrl}")
    private String loginUrl;

    @NoToken
    public void execute(String code, HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (CommonUtil.isEmpty(code)) {
            return;
        }

        String url = loginUrl + "/apollo/user/UserRpc/getLoginInfo.json?code=" + code;

        String json = HttpUtil.get(url);

        // json = EncryptUtil.aesDecode(json, key);

        // {"name":"王小波","email":"IML00466@iml.com","phone":0,"roleId":0,"roleName":"开发测试","deptIds":"[138]","deptName":"","searchKeywords":"王小波wangxiaobowxb","position":"架构师","status":"enable","openId":"IML00466","directDeptId":"138","gmtModified":"2022-12-29 18:31:23","modifier":"system","id":77}

        Map<String, Object> map = JsonUtil.fromJson(json, new TypeToken<Map<String, Object>>() {
        }.getType());

        LoginUser u = new LoginUser();
        u.setName(Convert.asString(map.get("name")));
        u.setUserId(Convert.asLong(map.get("id")));
        LoginCookieUtil.setLoginData(u, request, response);

        response.sendRedirect("/");
    }

    public static void main(String[] args) {
        String dd = EncryptUtil.aesEncode("json", key);
        System.out.println(dd);
        System.out.println(EncryptUtil.aesDecode(dd, key));
    }

}
