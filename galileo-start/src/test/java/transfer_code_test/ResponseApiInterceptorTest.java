package transfer_code_test;

import org.armada.galileo.open.adaptor.ResponseApiInterceptor;
import org.armada.galileo.exception.BizException;

/**
 * @author xiaobo
 * @date 2021/11/17 3:52 下午
 */
public class ResponseApiInterceptorTest implements ResponseApiInterceptor {

    // 客户系统回传地址
    private String customerUrl = "http://172.18.32.59:8081";

    @Override
    public String requestCustomer(String customerHttpUrl, String inputJson) throws Exception {
        return "OK";
//        String res = HttpUtil.postJson(customerUrl, inputJson, null);
//        return res;
    }

    @Override
    public void checkResponse(String customerJsonResult) throws Exception {
        // 随机异常
        if (Math.random() > 0.5) {
            throw new BizException("模拟随机异常 test error");
        }

    }
}