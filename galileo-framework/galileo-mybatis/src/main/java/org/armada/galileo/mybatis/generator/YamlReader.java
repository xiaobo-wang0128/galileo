//package org.armada.galileo.mybatis.generator;
//
//import java.io.InputStream;
//import java.util.HashMap;
//import java.util.Map;
//
//import org.armada.galileo.common.util.JsonUtil;
//import org.yaml.snakeyaml.Yaml;
//
//
//import lombok.extern.slf4j.Slf4j;
//
//@Slf4j
//public class YamlReader {
//
//	private static Map<String, Object> properties = new HashMap<>();
//
//	/**
//	 * 单例
//	 */
//	public static final YamlReader instance = new YamlReader();
//
//	static {
//		Yaml yaml = new Yaml();
//
//		InputStream in = null;
//		try {
//			in = YamlReader.class.getClassLoader().getResourceAsStream("application.yml");
//			properties = yaml.loadAs(in, HashMap.class);
//			System.out.println(JsonUtil.toJsonPretty(properties));
//
//		} catch (Exception e) {
//			log.error("Init yaml failed !", e);
//		} finally {
//			if (in != null) {
//				try {
//					in.close();
//				} catch (Exception e2) {
//				}
//			}
//		}
//	}
//
//	public static void main(String[] args) {
//
//	}
//
//	public String getValueByKey(String key) {
//		String separator = ".";
//		String[] separatorKeys = null;
//		if (key.contains(separator)) {
//			separatorKeys = key.split("\\.");
//		} else {
//			return properties.get(key).toString();
//		}
//
//		Map<String, Object> finalValue = new HashMap<>();
//		for (int i = 0; i < separatorKeys.length - 1; i++) {
//			if (i == 0) {
//				finalValue = (Map) properties.get(separatorKeys[i]);
//				continue;
//			}
//			if (finalValue == null) {
//				break;
//			}
//			finalValue = (Map) finalValue.get(separatorKeys[i]);
//		}
//		return finalValue == null ? null : (finalValue.get(separatorKeys[separatorKeys.length - 1]).toString());
//	}
//
//}