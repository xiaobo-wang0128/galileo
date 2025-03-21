package org.armada.galileo.sample.biz_config;

import lombok.Data;
import org.armada.galileo.autoconfig.AutoConfigGalileo;
import org.armada.galileo.autoconfig.annotation.ConfigField;
import org.armada.galileo.autoconfig.annotation.ConfigGroup;
import org.springframework.context.annotation.Configuration;

/**
 * @author buhuo.cai
 * @date 2022/1/28 7:53
 */
// @Configuration
@Data
@ConfigGroup(group = "理库模块配置", sort = 5)
public class IwmsSysConfigRelocation implements AutoConfigGalileo {

    /*=====================盘点相关配置==============================*/

    @ConfigField(name = "理库 - 目标料箱格口商品种类数量限制", desc = "目标料箱同一个格口限制不同SKU的种类数量，默认限制5种SKU")
    public Integer RELOCATION_TARGET_SUB_CONTAINER_SKU_QTY = 5;

}
