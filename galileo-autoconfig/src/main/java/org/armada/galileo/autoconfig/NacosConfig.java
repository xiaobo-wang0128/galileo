package org.armada.galileo.autoconfig;

import lombok.Data;

@Data
public class NacosConfig {

	/**
	 * 表单id
	 */
	private String configId;

	/**
	 * 表单定义
	 */
	private String configValue;

	public NacosConfig(String configId, String configValue) {
		this.configId = configId;
		this.configValue = configValue;
	}

	public NacosConfig() {
	}
	
}
