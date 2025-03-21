package test.user;

import com.google.gson.reflect.TypeToken;
import org.armada.galileo.common.util.CommonUtil;
import org.armada.galileo.common.util.Convert;
import org.armada.galileo.common.util.JsonUtil;

import java.util.*;

/**
 * @author xiaobo
 * @date 2022/2/7 6:22 下午
 */
public class UserMenuTreeTest {

    public static void main(String[] args) {
        String path = "/Users/wangxiaobo/Downloads/菜单数据.json";
        byte[] bufs = CommonUtil.readFileFromLocal(path);

        String json = new String(bufs);

        Map<String, Object> map = JsonUtil.fromJson(json, new TypeToken<Map<String, Object>>() {
        }.getType());

        List<Map<String, Object>> list = new ArrayList<>();

        Map<String, Object> tmp = (Map<String, Object>) map.get("data");

        List<Map<String, Object>> results = (List<Map<String, Object>>) tmp.get("results");

        for (Map<String, Object> app : results) {

            Map<String, Object> module = new LinkedHashMap<>();

            module.put("module", app.get("title"));
            List<Object> pages = new ArrayList<>();

            List<Map<String, Object>> children = (List<Map<String, Object>>) app.get("children");
            if (CommonUtil.isNotEmpty(children)) {
                module.put("children", pages);
                list.add(module);
                for (Map<String, Object> child : children) {
                    pages.add(getChild(child));
                }
            }

        }

        String out = JsonUtil.toJsonPretty(list);

        System.out.println(out);

    }

    public static Object getChild(Map<String, Object> map) {

        Integer type = Convert.asInt(map.get("type"));

        if (type == 2) {
            Map<String, Object> appPage = new LinkedHashMap<>();
            appPage.put("name", map.get("title"));
            appPage.put("path", map.get("path"));
            appPage.put("description", map.get("description"));
            List<Map<String, Object>> children = (List<Map<String, Object>>) map.get("children");
            if (CommonUtil.isNotEmpty(children)) {
                List<Object> subs = new ArrayList<>();

                for (Map<String, Object> child : children) {
                    subs.add(getChild(child));
                }

                if (subs.size() > 0) {
                    if (((Map) subs.get(0)).get("code") != null) {
                        appPage.put("privs", subs);
                    } else {
                        appPage.put("children", subs);
                    }
                }

            }

            return appPage;
        }

        if (type == 3) {
            Map<String, Object> btn = new LinkedHashMap<>();
            btn.put("label", map.get("title"));
            btn.put("description", map.get("description"));

            String code = ((String) map.get("permissions")).replaceAll("/", "_");
            code = code.substring(1);
            btn.put("code", code);
            btn.put("access", CommonUtil.asList(map.get("permissions")));
            return btn;

        }
        return null;
    }


}
