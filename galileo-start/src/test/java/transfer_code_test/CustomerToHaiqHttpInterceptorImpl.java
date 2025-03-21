package transfer_code_test;

import lombok.extern.slf4j.Slf4j;
import org.armada.galileo.common.util.JsonUtil;
import org.armada.galileo.open.adaptor.RequestApiInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * @author xiaobo
 * @date 2021/11/17 10:41 上午
 */
@Slf4j
public class CustomerToHaiqHttpInterceptorImpl implements RequestApiInterceptor {

    @Override
    public String route(String uri, HttpServletRequest request) {
        return uri;
    }

    @Override
    public void preCheck(String apiUrl, String inputJson, HttpServletRequest request) {
        log.info("[adaptor] pre check, apiUrl:{}, inputJson:{}", apiUrl, inputJson);

    }

    @Override
    public String afterSuccess(String apiUrl, String outputJson, HttpServletResponse response) {
        System.out.println("CustomerToHaiqHttpInterceptorImpl.afterCompletion");
        try {
            response.getOutputStream().write("ok".getBytes("utf-8"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "ok";
    }

    @Override
    public String afterException(String apiUrl, Integer errorCode, String errorMsg, HttpServletResponse response) {

        Map<String, Object> error = new HashMap<>();
        error.put("errorCode", errorCode);
        error.put("errorMsg", errorMsg);

        String responseJson = JsonUtil.toJson(error);
        try {
            response.getOutputStream().write(responseJson.getBytes("utf-8"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return responseJson;
    }
}