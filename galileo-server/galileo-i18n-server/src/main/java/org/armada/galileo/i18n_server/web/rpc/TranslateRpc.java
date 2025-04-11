package org.armada.galileo.i18n_server.web.rpc;

import org.armada.galileo.annotation.mvc.NoToken;
import org.armada.galileo.common.util.JsonUtil;
import org.armada.galileo.i18n_server.scheduler.AutoTranslate;
import org.armada.galileo.i18n_server.xunfei_api.XunFeiUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author xiaobo
 * @date 2023/9/6 21:20
 */
@Controller
public class TranslateRpc {

    @NoToken
    public Object doTranslate(String input, String jobType) throws Exception {
        return XunFeiUtil.doTranslate4Zh(input, jobType);
    }


    @Autowired
    private AutoTranslate autoTranslate;

    public void auto() {
        autoTranslate.doJob();
    }


    public static void main(String[] args) {
        String text = "这是一段包含中文的文本。";


        List<String> list = new ArrayList<>();
        list.add("这是一段包含中文的文本1");
        list.add("这是一段包含中文的文本1111");
        list.add("这是一段包含中文的文本11");
        list.add("这是一段包含中文的文本");
        list.add("这是一段包含中文的文本111111");

        Collections.sort(list, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o1.length() < o2.length() ? 1 : -1;
            }
        });

        System.out.println(JsonUtil.toJsonPretty(list));

//
//
//        String regex = "[\\u4e00-\\u9fa5]+";
//
//        Pattern pattern = Pattern.compile(regex);
//
//        Matcher matcher = pattern.matcher(text);
//
//        while (matcher.find()) {
//            System.out.println(matcher.group());
//        }
    }
}
