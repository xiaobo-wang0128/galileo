package org.armada.galileo.sample.biz_config.sample_config;


import java.util.ArrayList;
import java.util.List;

import org.armada.galileo.autoconfig.AutoConfigGalileo;
import org.armada.galileo.autoconfig.annotation.ConfigField;
import org.armada.galileo.autoconfig.annotation.ConfigGroup;
import org.armada.galileo.autoconfig.annotation.ConfigOption;
import org.armada.galileo.autoconfig.annotation.Option;
import org.springframework.context.annotation.Configuration;


import lombok.Data;

// @Configuration
@Data
@ConfigGroup(group = "工作站作业参数配置", sort = 999)
public class SysBizConfig implements AutoConfigGalileo {

	@ConfigField(name = "出库拣选空箱处理方式", options = PickEmptyContainerChargeTypeOptions.class)
	private String pickEmptyContainerChargeType = "N";

	@ConfigField(name = "短拣库存是否生成盘点", options = ShortPickAutoStocktackOptions.class)
	private String shortPickAutoStocktack = "N";

	@ConfigField(name = "理库库存是否生成盘点", options = ShortPickAutoStocktackOptions.class)
	private String regionAutoStocktack = "N";

	@ConfigField(name = "允许短缺完成订单")
	private Boolean Partial_Picking_Close = false;

	@ConfigField(name = "短拣密码验证")
	private String Picking_Lost_password = "";

	
	@ConfigField(name = "停止接单密码")
	private String Stop_Picking_password = "";

	
	public static class PickEmptyContainerChargeTypeOptions implements ConfigOption {
		@Override
		public List<Option> getOptions() {
			List<Option> options = new ArrayList<Option>();
			options.add(new Option("提醒取出并要求扫描空箱编码", "Y"));
			options.add(new Option("不提醒，空箱回库", "N"));
			return options;
		}
	};

	public static class ShortPickAutoStocktackOptions implements ConfigOption {
		@Override
		public List<Option> getOptions() {
			List<Option> options = new ArrayList<Option>();
			options.add(new Option("生成异常记录，处理丢失库存，生成盘点单", "Y"));
			options.add(new Option("生成异常记录，处理丢失库存", "N"));
			return options;
		}
	};

	public static class RegionAutoStocktack implements ConfigOption {
		@Override
		public List<Option> getOptions() {
			List<Option> options = new ArrayList<Option>();
			options.add(new Option("生成异常记录，处理丢失库存，生成盘点单", "Y"));
			options.add(new Option("生成异常记录，处理丢失库存", "N"));
			return options;
		}
	};

}
