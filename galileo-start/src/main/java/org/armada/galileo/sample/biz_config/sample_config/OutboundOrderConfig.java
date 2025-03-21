package org.armada.galileo.sample.biz_config.sample_config;



import org.armada.galileo.autoconfig.AutoConfigGalileo;
import org.armada.galileo.autoconfig.annotation.ConfigField;
import org.armada.galileo.autoconfig.annotation.ConfigGroup;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

// @Configuration
@Data
@ConfigGroup(group = "出库策略配置", sort = 1)
public class OutboundOrderConfig implements AutoConfigGalileo {

	@ConfigField(name = "出库规则")
	private Boolean empty = true;

	@ConfigField(name = "最大数量")
	private Integer maxPickNum =3;

}
