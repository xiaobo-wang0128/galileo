//package org.armada.galileo.sample.biz_config;
//
//import lombok.Data;
//import org.armada.galileo.autoconfig.AutoConfigGalileo;
//import org.armada.galileo.autoconfig.annotation.ConfigField;
//import org.armada.galileo.autoconfig.annotation.ConfigGroup;
//import org.armada.galileo.autoconfig.annotation.ConfigOption;
//import org.armada.galileo.autoconfig.annotation.Option;
//import org.armada.galileo.common.util.CommonUtil;
//import org.springframework.context.annotation.Configuration;
//
//import java.util.List;
//import java.util.stream.Collectors;
//import java.util.stream.Stream;
//
///**
// * @author xiaobo
// * @date 2021/11/12 4:22 下午
// */
////@Configuration
//@Data
//@ConfigGroup(group = "出库模块配置", sort = 2)
//public class IwmsSysConfigOutbound implements AutoConfigGalileo {
//
//
//    @ConfigField(name = "出库 - 客户订单号校验策略", desc = "客户订单下发时，是否需要校验客户号重复")
//    public Boolean OUTBOUND_ORDER_GLOBAL_UNIQUE_CHECK = false;
//
//
//    @ConfigField(name = "出库 - 差异是否自动创建调整单", desc = "当出库有异常登记，库存发生异常变动时自动生成库存调整单，如果差异需要上报，则不会自动创建调整单")
//    public Boolean OUTBOUND_CREATE_ADJUSTMENT_AUTO = false;
//
//
//
//
//    @ConfigField(name = "空箱出库 - 指定空箱口stationCode", desc = "空箱流转到指定的位置")
//    public String OUTBOUND_EMPTY_CONTAINER_SPECIAL_STATIONCODE = "";
//
//
//    @ConfigField(name = "出库 - 出库单取消是否按照订单组取消", desc = "表示是否一定要整组取消，判断 customerGroupNo 下面的所有订单是否都能取消。如果设置为 true 的话，即整个 customerGroupNo 组下面所有的订单都满足取消条件的时候，才能取消。默认值为 false。")
//    public Boolean OUTBOUND_CANCEL_BY_GROUP = false;
//
//    @ConfigField(name = "出库 - 封箱提醒", desc = "封箱提醒，true:提醒 false:不提醒")
//    public Boolean SEAL_REMINDER_SYSTEM_CONFIG_CODE = false;
//
//
//    @ConfigField(name = "出库 - 封箱回传是否启用波次最后一箱标识", desc = "波次维度的全局最后一箱，而不是拆箱的最后一箱")
//    public Boolean CALCULATE_LAST_CONTAINER_OF_CUSTOMER_GROUP = false;
//
//    @ConfigField(name = "出库 - 出库周转箱时效检查", desc = "周转箱需要多长时间内自动释放（分钟）")
//    public Integer OUTBOUND_INTERNAL_CHECK_TIME = 0;
//
//    @ConfigField(name = "出库 - 出库订单缺货等待的分钟", desc = "出库订单缺货等待的分钟")
//    public Integer OUTBOUND_ORDER_SHORT_WAITING_MINUTE = 600;
//
//    @ConfigField(name = "出库 - 出库周转箱是否客户控制释放", desc = "出库周转箱是否客户控制释放,true:是 false:否")
//    public Boolean OUTBOUND_TURNOVER_CONTAINER_WHETHER_CUSTOMER_CONTROL = false;
//
//    @ConfigField(name = "出库 - 出库周转箱是否自动创建", desc = "出库周转箱是否自动创建,true:是 false:否")
//    public Boolean OUTBOUND_TURNOVER_CONTAINER_AUTO_CREATE = false;
//
//    @ConfigField(name = "出库 - 是否同步修改同外部波次号下所有订单优先级", desc = "修改某一出库订单优先级时，是否同步修改与该订单相同外部波次号的所有订单的优先级，默认为False，即不同步修改")
//    public Boolean OUTBOUND_ORDER_SYNC_ADJUST_PRIORITY_WITH_SAME_CUSTOMER_GROUP_NO = Boolean.FALSE;
//
//
//}
