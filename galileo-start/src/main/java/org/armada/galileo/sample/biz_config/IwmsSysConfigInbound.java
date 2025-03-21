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
//import java.util.ArrayList;
//import java.util.List;
//
///**
// * @author xiaobo
// * @date 2021/11/12 4:22 下午
// */
//// @Configuration
//@Data
//@ConfigGroup(group = "入库模块配置", sort = 1)
//public class IwmsSysConfigInbound implements AutoConfigGalileo {
//
//    /**
//     * 入库客户订单重复校验
//     */
//    @ConfigField(name = "入库 - 客户订单重复校验")
//    public Boolean INBOUND_DUPLICATE_CUSTOMER_NUMBER_CHECK = true;
//
//    /**
//     * 入库LPN重复校验
//     */
//    @ConfigField(name = "入库 - LPN重复校验")
//    public Boolean INBOUND_DUPLICATE_LPN_CHECK = true;
//
//
//    /**
//     * 入库回传时机： 满箱、 上架成功
//     */
//    @ConfigField(name = "入库 - 入库回传时机", options = InboundNotifyTime.class)
//    public String inboundFeedbackTime = "putaway";
//
//
//    /**
//     * 入库异常登记
//     */
//    @ConfigField(name = "入库异常登记字段配置", desc = "异常登记字段配置")
//    public String abnormalFieldConfig;
//
//    /**
//     * 料箱最大承重(KG)
//     */
//    @ConfigField(name = "料箱最大承重(G)")
//    public Integer CONTAINER_MAX_LOAD_WEIGHT = 40000;
//
//
//    public static class InboundNotifyTime implements ConfigOption {
//        @Override
//        public List<Option> getOptions() {
//            List<Option> options = new ArrayList<Option>();
//            options.add(new Option("满箱成功回传", "full"));
//            options.add(new Option("上架成功回传", "putaway"));
//            return options;
//        }
//    }
//
//    public static class InboundNotifyType implements ConfigOption {
//        @Override
//        public List<Option> getOptions() {
//            List<Option> options = new ArrayList<Option>();
//            options.add(new Option("按箱回传", "byTote"));
//            options.add(new Option("按单回传", "byOrder"));
//            options.add(new Option("按订单明细", "byOrderDetail"));
//            return options;
//        }
//    }
//
//
//}
