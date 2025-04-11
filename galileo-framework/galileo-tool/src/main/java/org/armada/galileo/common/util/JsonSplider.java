package org.armada.galileo.common.util;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * @author xiaobo
 * @date 2023/2/23 20:39
 */
public class JsonSplider {


    public static void main(String[] args) {

        String json = new String(CommonUtil.readFileFromLocal("/Users/wangxiaobo/project/_codes/aml_2022/bronze.git/bronze-oms/oms-web/src/main/resources/open_api.json"), StandardCharsets.UTF_8);

        Map<String, String> map = new HashMap<>();

        char[] chars = json.toCharArray();

        boolean start = false;
        boolean end = false;

        StringBuilder tmpKey = null;
        StringBuilder tmpValue = null;

        String key = null;
        String value = null;

        Stack<Character> stack = new Stack<Character>();

        boolean valueStart = false;

        ElementType elementType = null;

        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];

            if (i != 0 && c == '"' && chars[i - 1] == '\\') {
                continue;
            }

            if (c == '{' && !start) {
                start = true;
                continue;
            }

            if (!start) {
                continue;
            }

            if ('"' == c) {
                if (tmpKey == null) {
                    tmpKey = new StringBuilder();
                    continue;
                }

                if (tmpKey != null) {
                    key = tmpKey.toString();
                    tmpKey = null;
                    map.put(key, value);
                    continue;
                }

                if (tmpValue != null) {
                    value = tmpValue.toString();

                    map.put(key, value);

                    tmpKey = null;
                    tmpValue = null;

                    key = null;
                    value = null;
                }
                continue;
            }

            if (':' == c) {
                if (tmpValue == null) {
                    // tmpValue = new StringBuilder();
                    valueStart = true;
                    continue;
                }
            }

            if ('{' == c) {
                if (tmpValue == null) {
                    tmpValue = new StringBuilder();
                    elementType = ElementType.Object;
                    continue;
                }
            }
            if ('}' == c) {

            }

            if ('[' == c) {
                if (tmpValue == null) {
                    tmpValue = new StringBuilder();
                    elementType = ElementType.Array;
                    continue;
                }
            }
            if (']' == c) {

            }

            if (',' == c) {

            }

            if (tmpKey != null) {
                tmpKey.append(c);
            }
            if (tmpValue != null) {
                tmpValue.append(c);
            }

        }


        for (Map.Entry<String, String> entry : map.entrySet()) {
            System.out.println(entry.getKey() + " : " + entry.getValue());
        }

    }

    private static enum ElementType {
        Object,

        Array,

        String,

        Number
    }
}
