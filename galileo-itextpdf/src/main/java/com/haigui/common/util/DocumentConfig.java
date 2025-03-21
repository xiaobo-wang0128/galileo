package com.haigui.common.util;

import java.io.File;
import java.net.URL;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import com.haigui.common.pdf.util.help.ClassHelper;
import lombok.extern.slf4j.Slf4j;
import org.armada.galileo.common.loader.ConfigLoader;
import org.armada.galileo.common.util.CommonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Slf4j
public class DocumentConfig {

	public static Map<String, String> localConfig = new LinkedHashMap<String, String>();

	private static List<String> config_property = CommonUtil.asList("application.properties");

	static {
		try {

			String springProfile = System.getProperties().getProperty("spring.profiles.active");
			if (springProfile != null) {
				config_property.add("application-" + springProfile + ".properties");
			}

			for (String cfg : config_property) {
				Properties p = ConfigLoader.loadProperties(cfg, false, false);
				readPropertiesConfig(p);
			}

		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}


	}

	private static void readPropertiesConfig(Properties p) {
		for (Entry<Object, Object> entry : p.entrySet()) {
			String valur = entry.getValue().toString().trim();
			String key = entry.getKey().toString().trim();
			localConfig.put(key, valur);
		}
	}

	/**
	 * 获取配置项的值
	 *
	 * @param key
	 * @return
	 */
	public static String getConfig(String key) {
		return localConfig.get(key);
	}

	/**
	 * 获取配置项的值
	 *
	 * @param key
	 * @return
	 */
	public static String getConfig(String key, String defaultValue) {
		String v = localConfig.get(key);
		if (v != null) {
			return v;
		}
		return defaultValue;
	}

	public static String getAppHomePath() {
		try {
			String springProfile = System.getProperties().getProperty("spring.profiles.active");

			String activeCfgFle = "application-" + springProfile + ".properties";

			log.info("[srping-config] springProfile: " + activeCfgFle);

			Enumeration<URL> en = ClassHelper.getClassLoader().getResources(activeCfgFle);

			if (en.hasMoreElements()) {

				String path = en.nextElement().getPath();
				if (path != null) {

					if ("\\".equals(File.separator)) {
						path = CommonUtil.replaceAll(path, "/", "\\");
						if (path.startsWith("\\")) {
							path = path.substring(1);
						}
					}

					path = path.substring(0, path.lastIndexOf(File.separator));

					if (path.endsWith("conf")) {
						path = path.substring(0, path.lastIndexOf(File.separator));
					}

					return path;
				}
			}

			return null;

		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return null;
		}
	}

}
