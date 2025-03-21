package org.armada.galileo.facade.web.screen;

import org.armada.galileo.annotation.mvc.NoToken;
import org.springframework.stereotype.Controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author xiaobo
 * @date 2022/12/29 15:45
 */
@Controller
public class Mock {

    @NoToken
    public void execute(String code, HttpServletRequest request, HttpServletResponse response) throws Exception {

    }

}
