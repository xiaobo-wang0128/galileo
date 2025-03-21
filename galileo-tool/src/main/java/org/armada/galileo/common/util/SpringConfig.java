package org.armada.galileo.common.util;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.armada.galileo.common.loader.SpringMvcUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SpringConfig {

	public static Map<String, String> localConfig = new LinkedHashMap<String, String>();

	private static List<String> config_property = CommonUtil.asList("application.properties");

	private static Logger log = LoggerFactory.getLogger(SpringConfig.class);

//	static {
//		try {
//			String springProfile = System.getProperties().getProperty("spring.profiles.active");
//			if (springProfile != null) {
//				config_property.add("application-" + springProfile + ".properties");
//			}
//
//			for (String cfg : config_property) {
//				Properties p = ConfigLoader.loadProperties(cfg, false, false);
//				readPropertiesConfig(p);
//			}
//		} catch (Exception e) {
//			log.error(e.getMessage(), e);
//		}
//
//	}


	/**
	 * 获取配置项的值
	 *
	 * @param key
	 * @return
	 */
	public static String getConfig(String key) {
		if(SpringMvcUtil.getContext()!=null){
			return SpringMvcUtil.getContext().getEnvironment().getProperty(key);
		}
		return null;
	}

	/**
	 * 获取配置项的值
	 *
	 * @param key
	 * @return
	 */
	public static String getConfig(String key, String defaultValue) {
		String v = getConfig(key);
		if (v != null) {
			return v;
		}
		return defaultValue;
	}

	public static String getAppHomePath() {
		try {
			String springProfile = System.getProperties().getProperty("spring.profiles.active");

//			String activeCfgFle = "application-" + springProfile + ".properties";
//
//			log.info("[srping-config] springProfile: " + activeCfgFle);

//			Enumeration<URL> en = ClassHelper.getClassLoader().getResources(activeCfgFle);
//
//			if (en.hasMoreElements()) {
//
//				String path = en.nextElement().getPath();
//				if (path != null) {
//
//					// windows
//					if ("\\".equals(File.separator)) {
//						path = CommonUtil.replaceAll(path, "\\", "/");
//					}
//
//					// String osName = System.getProperty("os.name");
//					// if(osName!=null && osName.startsWith("/")) {
//					//
//					// }
//
//					if (path.endsWith("/")) {
//						path = path.substring(0, path.length() - 1);
//					}
//
//					path = path.substring(0, path.lastIndexOf("/"));
//
//					if (path.endsWith("conf")) {
//						path = path.substring(0, path.lastIndexOf("/"));
//					}
//
//					log.info("[srping-config] app home-path: " + path);
//
//					return path;
//				}
//			}

			return null;

		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return null;
		}
	}

	public static void main(String[] args) {
		System.out.println(System.getProperty("os.name"));
	}
}
