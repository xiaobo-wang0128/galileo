import org.armada.galileo.common.util.CommonUtil;
import org.armada.galileo.common.util.HttpUtil;
import org.armada.galileo.mvc_plus.encrypt.EncryptUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * @author xiaobo
 * @date 2023/11/26 19:53
 */
public class LoginTest {

    private static final String key = "i187a091e760f96g";

    public static void main(String[] args) {

        String loginId = "gaoqiq";
        String pwd = "653604";

        pwd = CommonUtil.md5(pwd);

        loginId = EncryptUtil.aesEncodeBase64(loginId, key);
        pwd = EncryptUtil.aesEncodeBase64(pwd, key);

        Map<String, String> map = new HashMap<>();

        map.put("loginId", loginId);
        map.put("pwd", pwd);

        String loginUrl = "http://192.168.4.249:8001/ums-api/user/UserInfoRpc/login_tms_app.json";
        String json = HttpUtil.post(loginUrl, map);

        System.out.println(json);

    }

}
