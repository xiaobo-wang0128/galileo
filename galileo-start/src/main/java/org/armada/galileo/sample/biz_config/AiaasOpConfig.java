//package org.armada.galileo.sample.biz_config;
//
//import org.armada.galileo.autoconfig.AutoConfigGalileo;
//import org.armada.galileo.autoconfig.annotation.ConfigField;
//import org.armada.galileo.autoconfig.annotation.ConfigGroup;
//import org.springframework.context.annotation.Configuration;
//
//import lombok.Data;
//
//@Configuration("aiaasConfig")
//@Data
//@ConfigGroup(group = "算法策略配置", sort = 1)
//public class AiaasOpConfig implements AutoConfigGalileo {
//
//	@ConfigField(name = "是否允许组单")
//	public Boolean ALLOW_ORDER_COMBINATION = false;// ("opConfig.allowOrderCombination", "是否允许组单",""),
//
//	@ConfigField(name = "单品订单的组单最大总件数")
//	public Integer MAX_QUANTITY_FOR_SINGLE_ORDER_COMBINATION = 1000;// ("orderCombinationConfig.maxQuantityForSingleOrderCombination",
//
//	@ConfigField(name = "多品订单的组单最大总件数")
//	public Integer MAX_QUANTITY_FOR_MULTI_ORDER_COMBINATION = 1000;// ("orderCombinationConfig.maxQuantityForMultiOrderCombination",
//
//	@ConfigField(name = "多品订单的组单最大订单数量")
//	public Integer MAX_ORDERS_QUANTITY_FOR_MULTI_ORDER_COMBINATION = 100;// ("orderCombinationConfig.maxOrdersQuantityForMultiOrderCombination",
//
//
//	@ConfigField(name = "使用集中模式")
//	public Boolean CONCENTRATED_CLUSTERING_MODE;// ("opConfig.concentratedClusteringMode", "是否使用集中模式",""),
//
//	@ConfigField(name = "使用ALNS算法来优化库存分配", desc = "ALNS算法会考虑多个因素来综合优化库存分配")
//	public Boolean USE_ALNS_ALGORITHM;// ("opConfig.useALNSAlgorithm", "是否使用ALNS算法来优化库存分配，ALNS算法会考虑多个因素来综合优化库存分配",""),
//	// "件数均衡因素",""),
//
//	@ConfigField(name = "允许操作台集合单追加", desc = "允许追加订单到操作台上的已组好的集合单")
//	public Boolean ALLOW_ADD_ADDITIONAL_ORDERS_INTO_COMBINED_ORDERS;// ("opConfig.allowAddAdditionalOrdersIntoCombinedOrders",
//
//	@ConfigField(name = "开启由外部显式的通知某个订单完成")
//	public Boolean EXPLICITLY_NOTIFY_ORDER_IS_FINISH;// ("opConfig.explicitlyNotifyOrderIsFinish", "是否开启由外部显式的通知某个订单完成",""),
//
//	@ConfigField(name = "优先挑选件数和种类少的订单", desc = "true 时优先挑选件数和种类少的订单,false时优先挑选件数和种类多的订单")
//	public Boolean DISPATCH_LESS_AMOUNT_FIRST;// ("opConfig.dispatchLessAmountFirst",
//												// "改变挑单时不同订单的优先级，true时优先挑选件数和种类少的订单；false时优先挑选件数和种类多的订单",""),
//
//	@ConfigField(name = "库存同步接口", desc = "AIaaS通过该接口初始化OP算法库存信息")
//	public String STOCK_SYNC_URI;// ("syncDataUrlconfig.stockSyncUri", "库存同步接口，AIaaS通过该接口初始化OP算法库存信息",""),
//
//	@ConfigField(name = "操作台配置同步接口", desc = "AIaaS通过该接口初始化OP算法操作台信息")
//	public String STATION_CONFIG_SYNC_URI;// ("syncDataUrlconfig.stationConfigSyncUri",
//											// "操作台配置同步接口，AIaaS通过该接口初始化OP算法操作台信息",""),
//
//	@ConfigField(name = "箱子基本信息同步接口", desc = "AIaaS通过该接口初始化OP算法料箱信息")
//	public String BIN_INFO_SYNC_URI;// ("syncDataUrlconfig.binInfoSyncUri", "箱子基本信息同步接口，AIaaS通过该接口初始化OP算法料箱信息",""),
//														// "单品订单的组单最大总件数","12345"),
//
//	@ConfigField(name = "容器最大体积", desc = "组的集合单货物总体积不能大于给定的容器体积")
//	public Integer CONTAINER_VOLUME;// ("orderCombinationConfig.containerVolume", "容器体积，组的集合单货物总体积不能大于给定的容器体积",""),
//
//	@ConfigField(name = "允许的最大机器人数量")
//	public Integer MAX_KUBOT_QUANTITY_FOR_EACH_DESTINATION = Integer.MAX_VALUE;// ("stationConfig.maxKubotQuantityForEachDestination",
//
//	@ConfigField(name = "优先清空低库存料箱因素的权重")
//	public Double CONTAINER_RELATED_AVAILABLE_STOCK_WEIGHT = 0.1; // ("opConfig.containerRelatedAvailableStockWeight", "设置优先清空低库存料箱因素的权重",""),
//
//	@ConfigField(name = "sku重取率因素权重")
//	public Double SKU_REFETCH_WEIGHT = 0.001;// ("opConfig.clusterAlgoSolutionEvaluationParameters.skuRefetchWeight",
//												// "sku重取率因素权重",""),
//
//	@ConfigField(name = "sku种类方差因素的权重")
//	public Double SKU_VARIANCE_WEIGHT = 0.002;// ("opConfig.clusterAlgoSolutionEvaluationParameters.skuVarianceWeight",
//												// "sku种类方差因素的权重",""),
//
//	@ConfigField(name = "放到sku种类方差因素的权重")
//	public Double ENLARGE_TIMES_FOR_SKU_VARIANCE_WEIGHT = 0.003;// ("opConfig.clusterAlgoSolutionEvaluationParameters.enlargeTimesForSkuVarianceWeight",
//																// "放到sku种类方差因素的权重",""),
//
//	@ConfigField(name = "sku库存区域命中因素权重")
//	public Double SKUS_WEIGHT_FOR_HIT_SCORE = 0.004;// ("opConfig.clusterAlgoSolutionEvaluationParameters.skusWeightForHitScore",
//													// "sku库存区域命中因素权重",""),
//
//	@ConfigField(name = "稀缺sku的分散惩罚因素权重")
//	public Double WEIGHT_FOR_SCARCE_SKU_PENALTY = 0.005;// ("opConfig.clusterAlgoSolutionEvaluationParameters.weightForScarceSkuPenalty",
//														// "稀缺sku的分散惩罚因素权重",""),
//
//	// ---
//	@ConfigField(name = "件数均衡因素")
//	public Double DEMAND_VARIANCE_WEIGHT;// ("opConfig.clusterAlgoSolutionEvaluationParameters.demandVarianceWeight",
//											// "是否允许追加订单到操作台上的已组好的集合单",""),
//
//	@ConfigField(name = "设置巷道方向", desc = "设置巷道方向平行于哪个坐标轴")
//	public String DEFAULT_ROADWAY_DIRECTION_PARALLEL_WITH_GIVEN_AXIS;// ("opConfig.defaultRoadwayDirectionParallelWithGivenAxis",
//																		// "设置巷道方向平行于哪个坐标轴",""),
//
//}
