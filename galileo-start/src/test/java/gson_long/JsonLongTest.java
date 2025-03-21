package gson_long;

import com.google.common.reflect.TypeToken;
import org.armada.galileo.common.util.JsonUtil;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author xiaobo
 * @date 2022/4/25 9:59 PM
 */
public class JsonLongTest {

    public static void main(String[] args) {
        Long id = 6346537648003686003L;

        Map<String, Long> s = new HashMap<>();
        s.put("aaa", id);

        String ss = JsonUtil.toJson(s);
        System.out.println(ss);

        s = JsonUtil.fromJson(ss, new TypeToken<Map<String, Long>>() {
        }.getType());

        System.out.println(s.get("aaa"));

        System.out.println(s.get("aaa").getClass());


    }
}
