package org.armada.galileo.mybatis.generator;

import lombok.extern.slf4j.Slf4j;
import org.armada.galileo.common.util.CommonUtil;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class YamlReader {

    private static Map<String, Object> properties = new HashMap<>();

    /**
     * 单例
     */
    public static final YamlReader instance = new YamlReader();

    static {
        Yaml yaml = new Yaml();

        InputStream in = null;
        try {
            in = YamlReader.class.getClassLoader().getResourceAsStream("application.yml");
            properties = yaml.loadAs(in, HashMap.class);

            for (Map.Entry<String, Object> entry : properties.entrySet()) {

                if (entry.getValue() instanceof Map) {

                    Map<String, Object> tmpMap = (Map<String, Object>) entry.getValue();

                } else {

                }

            }

        } catch (Exception e) {
            log.error("Init yaml failed !", e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (Exception e2) {
                }
            }
        }
    }

    public static void main(String[] args) {

    }

    public static String getValue(String key) {
        String value = getValueByKey(key);

        List<String> vars = CommonUtil.getMatchedStrs(value, "\\$\\{.*?\\}");

        if (vars != null && vars.size() > 0) {
            for (String var : vars) {

                String varKey = var.substring(2, var.length() - 1);

                value = CommonUtil.replaceAll(value, var, getValueByKey(varKey));
            }
        }
        return value;
    }

    private static String getValueByKey(String key) {
        String separator = ".";
        String[] separatorKeys = null;
        if (key.contains(separator)) {
            separatorKeys = key.split("\\.");
        } else {
            return properties.get(key).toString();
        }

        Map<String, Object> finalValue = new HashMap<>();
        for (int i = 0; i < separatorKeys.length - 1; i++) {
            if (i == 0) {
                finalValue = (Map) properties.get(separatorKeys[i]);
                continue;
            }
            if (finalValue == null) {
                break;
            }
            finalValue = (Map) finalValue.get(separatorKeys[i]);
        }
        return finalValue == null ? null : (finalValue.get(separatorKeys[separatorKeys.length - 1]).toString());
    }

}
