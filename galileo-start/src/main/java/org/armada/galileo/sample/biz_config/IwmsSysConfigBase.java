//package org.armada.galileo.sample.biz_config;
//
//import lombok.Data;
//import org.armada.galileo.autoconfig.AutoConfigGalileo;
//import org.armada.galileo.autoconfig.annotation.ConfigField;
//import org.armada.galileo.autoconfig.annotation.ConfigGroup;
//import org.springframework.context.annotation.Configuration;
//
///**
// * @author xiaobo
// * @date 2021/11/12 4:22 下午
// */
////@Configuration
//@Data
//@ConfigGroup(group = "基础通用配置", sort = 0)
//public class IwmsSysConfigBase implements AutoConfigGalileo {
//
//    /**
//     * 入库客户订单重复校验
//     */
//    @ConfigField(name = "扫描SKU后自动在输入框显示当前SKU剩余数量")
//    public Boolean autoLoadQuantity = true;
//
//    @ConfigField(name = "自动计算SKU体积", desc = "每次收货的时候计算SKU体积")
//    public Boolean CALCULATE_SKU_VOLUME_AUTO = false;
//
//    @ConfigField(name = "空箱下架的输送线编码", desc = "为了查询输送线上的料箱数量。如果有多个以,隔开")
//    public String CONVEYOR_CODES_FOR_EMPTY_OUTBOUND;
//
//    /**
//     * 输送线空箱数量阈值
//     */
//    @ConfigField(name = "输送线空箱数量阈值", desc = "当输送线上的空闲库位数量 - 未完成的空箱出库任务数量 > 当前值时，则创建空箱出库任务")
//    public Integer EMPTY_CONTAINER_THRESHOLD = 6;
//
//    @ConfigField(name = "上架任务超时(分钟)")
//    public Integer PUT_AWAY_TIMEOUT = 30;
//
//    @ConfigField(name = "二次确认料箱任务完成",desc = "料箱任务完成时，需要二次确认料箱才会离开")
//    public Boolean CONFIRM_CONTAINER_LEAVE = false;
//}
