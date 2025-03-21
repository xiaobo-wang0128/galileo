package org.armada.galileo.rainbow_gate.transfer.constant;

import org.armada.galileo.common.util.SpringConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RainbowGateConfig {

	private static Logger log = LoggerFactory.getLogger(RainbowGateConfig.class);
	/**
	 * 默认为内网部署
	 */
	private static boolean deployIsInner = true;

	static {

		String appPath = SpringConfig.getAppHomePath();
		if (appPath != null) {
			System.setProperty("haigui.app.home.path", appPath);
		}

		String springProfile = System.getProperties().getProperty("rainbow.deploy.env");
		if ("out".equals(springProfile)) {
			deployIsInner = false;
		}

	}

	// public static void deployToOut() {
	// log.info("[rainbow] 当前部署环境为外网模式 ");
	// deployIsInner = false;
	// }

	/**
	 * 是否为内网部署， 内网部署时，请求 GateClient 不校验 routeKey
	 * 
	 * @return
	 */
	public static boolean deployIsInner() {
		return deployIsInner;
	}

	private static String appName = null;

	public static void setAppName(String appName) {
		RainbowGateConfig.appName = appName;
	}

	public static String getAppName() {
		return appName;
	}
}
