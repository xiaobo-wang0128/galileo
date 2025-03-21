//package org.armada.galileo.sample.biz_config;
//
//import lombok.Data;
//import org.armada.galileo.autoconfig.AutoConfigGalileo;
//import org.armada.galileo.autoconfig.annotation.ConfigField;
//import org.armada.galileo.autoconfig.annotation.ConfigGroup;
//import org.springframework.context.annotation.Configuration;
//
///**
// * Description:
// * Created by tiejiang on 2022/1/31 10:29
// */
//// @Configuration
//@Data
//@ConfigGroup(group = "容器配置", sort = 6)
//public class IwmsSysConfigContainer implements AutoConfigGalileo {
//
//
//    @ConfigField(name = "容器 装商品规则", desc = "配置容器能装什么sku ：[{\"specType\":\"SUB_CONTAINER\",\"mutexConfigs\":[{\"goodCheckWay\":\"ITEM\",\"isMix\":true,\"amount\":2,\"calculateField\":null,\"isSupportPlugin\":null}]}]")
//    public String container_mutex_rule;
//
//    @ConfigField(name = "容器体积限制", desc = "容器体积限制")
//    public Boolean isLimitVolume = false;
//
//    @ConfigField(name = "容器体积的百分比配置", desc = "可装入商品容器的百分比配置")
//    public String containerMaxVolume;
//}
