package com.haigui.common.pdf.util.help;


import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigLoader {
	

	private static Logger log = LoggerFactory.getLogger(ConfigLoader.class);
	
	/**
	 * properties配置文件
	 * 
	 * @param fileName 文件路径，不能以 / 开头
	 * @param allowMultiFile 是否允许存在多个同名文件
	 * @param optional 文件是否可选
	 * @return
	 */
	public static Properties loadProperties(String fileName, boolean allowMultiFile, boolean optional) {
		Properties properties = new Properties();
		if (fileName.startsWith("/")) {
			try {
				FileInputStream input = new FileInputStream(fileName);
				try {
					// properties.load(input);
					properties.load(new InputStreamReader(input, "utf-8"));
				} finally {
					input.close();
				}
			} catch (Throwable e) {
				log.error("Failed to load " + fileName + " file from " + fileName + "(ingore this file): " + e.getMessage());
			}
			return properties;
		}

		List<java.net.URL> list = new ArrayList<java.net.URL>();
		try {
			Enumeration<java.net.URL> urls = ClassHelper.getClassLoader().getResources(fileName);
			list = new ArrayList<java.net.URL>();
			while (urls.hasMoreElements()) {
				list.add(urls.nextElement());
			}
		} catch (Throwable t) {
			log.error("Fail to load " + fileName + " file: " + t.getMessage(), t);
		}

		if (list.size() == 0) {
			if (!optional) {
				log.error("No " + fileName + " found on the class path.");
			}
			return properties;
		}

		if (!allowMultiFile) {
			if (list.size() > 1) {
				String errMsg = String.format("only 1 %s file is expected, but %d  files found on class path: %s", fileName, list.size(), list.toString());
				log.error(errMsg);
			}

			try {

				properties.load(new InputStreamReader(ClassHelper.getClassLoader().getResourceAsStream(fileName), "utf-8"));
			} catch (Throwable e) {
				log.error("Failed to load " + fileName + " file from " + fileName + "(ingore this file): " + e.getMessage());
			}
			return properties;
		}

		log.info("load " + fileName + " properties file from " + list);

		for (java.net.URL url : list) {
			try {
				Properties p = new Properties();
				InputStream input = url.openStream();
				if (input != null) {
					try {
						p.load(new InputStreamReader(input, "utf-8"));
						properties.putAll(p);
					} finally {
						try {
							input.close();
						} catch (Throwable t) {
						}
					}
				}
			} catch (Throwable e) {
				log.error("Fail to load " + fileName + " file from " + url + "(ingore this file): " + e.getMessage());
			}
		}

		return properties;
	}

	/**
	 * 文件对应的输入流
	 * 
	 * @param fileName 文件路径，不能以 / 开头
	 * @param allowMultiFile 是否允许存在多个同名文件
	 * @param optional 文件是否可选
	 * @return
	 */
	public static InputStream loadResource(String fileName, boolean allowMultiFile, boolean optional) {
		InputStream input = null;
		if (fileName.startsWith("/")) {
			try {
				input = new FileInputStream(fileName);
				return input;
			} catch (Throwable e) {
				log.debug("Failed to load " + fileName + " file from " + fileName + "(ingore this file): " + e.getMessage());
			}
		}

		List<java.net.URL> list = new ArrayList<java.net.URL>();
		try {
			Enumeration<java.net.URL> urls = ClassHelper.getClassLoader().getResources(fileName);
			list = new ArrayList<java.net.URL>();
			while (urls.hasMoreElements()) {
				list.add(urls.nextElement());
			}
		} catch (Throwable t) {
			log.debug("Fail to load " + fileName + " file: " + t.getMessage(), t);
		}

		if (list.size() == 0) {
			if (!optional) {
				// log.error("No " + fileName + " found on the class path.");
			}
			return input;
		}

		if (!allowMultiFile) {
			if (list.size() > 1) {
				String errMsg = String.format("only 1 %s file is expected, but %d  files found on class path: %s", fileName, list.size(), list.toString());
				log.debug(errMsg);
			}

			return ClassHelper.getClassLoader().getResourceAsStream(fileName);
		}

		for (java.net.URL url : list) {
			try {
				return url.openStream();
			} catch (Throwable e) {
				log.debug("Fail to load " + fileName + " file from " + url + "(ingore this file): " + e.getMessage());
			}
		}
		if (input == null) {
			throw new RuntimeException(fileName + "不存在");
		}
		return input;
	}
}
