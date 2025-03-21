package org.armada.galileo.i18n_server.web.rpc;

import org.armada.galileo.i18n_server.user.LoginUser;
import org.armada.galileo.i18n_server.user.ThreadUser;
import org.armada.galileo.i18n_server.util.LoginCookieUtil;
import org.armada.galileo.annotation.mvc.NoToken;
import org.springframework.stereotype.Controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author xiaobo
 * @date 2022/12/29 15:42
 */
@Controller
public class LoginRpc {

    @NoToken
    public void mock(HttpServletRequest request, HttpServletResponse response) {
        LoginUser u = new LoginUser();
        u.setName("test");
        u.setUserId(11L);
        LoginCookieUtil.setLoginData(u, request, response);
    }

    @NoToken
    public LoginUser getLogin(){
        return ThreadUser.get();
    }

}
