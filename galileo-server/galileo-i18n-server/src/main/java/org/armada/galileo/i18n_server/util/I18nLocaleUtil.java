package org.armada.galileo.i18n_server.util;

import com.google.gson.reflect.TypeToken;
import lombok.Data;
import lombok.experimental.Accessors;
import org.armada.galileo.common.util.CommonUtil;
import org.armada.galileo.common.util.JsonUtil;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author xiaobo
 * @date 2022/12/27 11:36
 */
public class I18nLocaleUtil {

    @Data
    @Accessors(chain = true)
    public static class I18nConfigItem {
        /**
         * 中文描述
         */
        private String label; //": "法文",
        /**
         * code
         */
        private String value;
        /**
         * code 对应的语种描述
         */
        private String desc;
    }

    static List<I18nConfigItem> lans = null;

    static Map<String, I18nConfigItem> codeCache = new HashMap<>();

    static Map<String, I18nConfigItem> labelCache = new HashMap<>();

    static {
        byte[] bufs = CommonUtil.readFileToBuffer("lan_all.json");
        String json = new String(bufs, Charset.forName("utf-8"));
        lans = JsonUtil.fromJson(json, new TypeToken<List<I18nConfigItem>>() {
        }.getType());

        for (I18nConfigItem lan : lans) {
            codeCache.put(lan.getValue(), lan);
            labelCache.put(lan.getLabel(), lan);
        }
    }


    /**
     * 获取 code 代表的语种描述
     *
     * @param code
     * @return
     */
    public static I18nConfigItem getLanByCode(String code) {
        return codeCache.get(code);
    }


    /**
     * 获取 code 代表的语种描述
     *
     * @param desc
     * @return
     */
    public static I18nConfigItem getLanByDesc(String desc) {
        return labelCache.get(desc);
    }
}
