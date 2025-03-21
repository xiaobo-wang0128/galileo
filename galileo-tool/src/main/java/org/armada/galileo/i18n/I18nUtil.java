package org.armada.galileo.i18n;

import org.armada.galileo.common.loader.ConfigLoader;
import org.armada.galileo.common.util.CommonUtil;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * @author xiaobo
 * @date 2023/1/4 18:25
 */
public class I18nUtil {


    /**
     * <pre>
     *
     * {
     * zh: {
     * key1: '',
     * key2: ''
     * } ,
     * en: {
     * key1: '',
     * key2: ''
     * }
     * }
     *
     *
     *
     * </pre>
     */
    private static Map<String, Map<String, String>> i18nInfoMap;


    private static String lans = "zh|en|ru|fr|ja|de|ko";


    static {

        i18nInfoMap = new ConcurrentHashMap<>();

        String head = "messages";
        Set<ResourceBundle> messagesProperties = new HashSet<>();
        Set<String> keys = new HashSet<>();
        for (String lan : lans.split("\\|")) {
            try {
                InputStream is = ConfigLoader.loadResource(head + "_" + lan + ".properties", false, false);

                if (is == null) {
                    continue;
                }

                Properties p = new Properties();
                p.load(new InputStreamReader(is, "utf-8"));


                Map<String, String> values = new HashMap<>();

                for (String pname : p.stringPropertyNames()) {
                    values.put(pname, p.get(pname).toString());
                }

                i18nInfoMap.put(lan, values);

                is.close();

            } catch (Exception ignored) {
            }
        }

    }



    /**
     * 从本地缓存获取单条配置
     *
     * @param locale       语言
     * @param key          key
     * @param defaultValue 默认值
     * @return value
     */
    public static String get(String locale, String key, String defaultValue) {
        if (locale == null || key == null) {
            return defaultValue;
        }

        Map<String, String> dictionary = i18nInfoMap.get(locale);
        if (dictionary == null) {
            return defaultValue;
        }
        String s = dictionary.get(key);
        return s == null ? defaultValue : s;
    }


    /**
     * 从本地缓存获取单条配置, locale 语言类型将从上下文获取
     *
     * @param key          key
     * @param defaultValue 默认值
     * @return value
     */
    public static String get(String key, String defaultValue) {
        String locale = I18nContext.getI18nContext();
        return get(locale, key, defaultValue);
    }


//    public static String get(I18nDictionary i18nDictionary){
////        clsName = clsName.substring(clsName.lastIndexOf(".") + 1);
////        String i18nKey = clsName + "__" + error.toString();
//    }


}
