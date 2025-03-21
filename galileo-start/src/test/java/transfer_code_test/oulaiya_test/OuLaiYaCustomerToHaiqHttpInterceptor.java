package transfer_code_test.oulaiya_test;

import com.google.common.reflect.TypeToken;
import org.armada.galileo.common.util.JsonUtil;
import org.armada.galileo.open.adaptor.RequestApiInterceptor;

import javax.servlet.http.*;
import java.util.Map;

/**
 * @author xiaobo
 * @date 2022/5/6 9:14 AM
 */
public class OuLaiYaCustomerToHaiqHttpInterceptor implements RequestApiInterceptor {

    public String route(String inputJson, HttpServletRequest request) {

        if (request.getRequestURI().startsWith("/open/api/loreal")) {

            System.out.println(request.getRequestURI());

            Map<String, Object> map = JsonUtil.fromJson(inputJson, new TypeToken<Map<String, Object>>() {
            }.getType());

            System.out.println(map.get("type-head"));

            // 入库创建
            if ("xxx".equals(map.get("type-head"))) {
                return "com.haiq.wms.client.api.RequestApi2.userCreate";
            }

            // 入库取消
            else if ("xxx".equals(map.get("type-head"))) {
                return "com.haiq.wms.client.api.RequestApi2.inboundCancel";
            }

        }
        return null;
    }

    @Override
    public void preCheck(String apiUrl, String inputJson, HttpServletRequest request) throws Exception {
    }

    @Override
    public String afterSuccess(String apiUrl, String outputJson, HttpServletResponse response) throws Exception {
        String responseMsg = "OK";
        try {
            response.getOutputStream().write(responseMsg.getBytes("utf-8"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return responseMsg;
    }

    @Override
    public String afterException(String apiUrl, Integer errorCode, String errorMsg, HttpServletResponse response) throws Exception {
        String responseMsg = errorMsg;
        try {
            response.getOutputStream().write(responseMsg.getBytes("utf-8"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return responseMsg;
    }

}