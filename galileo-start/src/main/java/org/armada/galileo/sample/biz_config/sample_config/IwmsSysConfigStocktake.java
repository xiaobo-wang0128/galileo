package org.armada.galileo.sample.biz_config.sample_config;

import lombok.Data;
import org.armada.galileo.autoconfig.AutoConfigGalileo;
import org.armada.galileo.autoconfig.annotation.ConfigField;
import org.armada.galileo.autoconfig.annotation.ConfigGroup;
import org.armada.galileo.autoconfig.annotation.ConfigOption;
import org.armada.galileo.autoconfig.annotation.Option;
import org.armada.galileo.common.util.CommonUtil;
import org.armada.galileo.common.util.JsonUtil;
import org.armada.galileo.exception.BizException;
import org.armada.galileo.sample.biz_config.sample_config.sample_constant.*;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author buhuo.cai
 * @date 2021/11/12 4:22 下午
 */
@Configuration
@Data
@ConfigGroup(group = "盘点模块配置", sort = 50, preCheck = true, afterModify = true)
public class IwmsSysConfigStocktake implements AutoConfigGalileo {


    public void preCheck() {
        if (STOCKTAKE_CREATE_LOGIC_STRATEGY && STOCKTAKE_CREATE_ADJUSTMENT_AUTO) {
            throw new BizException("盘点 - 是否按逻辑区创建盘点单， 盘点 - 盘点差异是否自动创建调整单  不能同时为 true");
        }
    }

    public void afterModify() {
        System.out.println("after:");
        System.out.println(JsonUtil.toJson(this));
    }

    /*=====================盘点相关配置==============================*/

    @ConfigField(name = "盘点 - 是否按逻辑区创建盘点单", desc = "创建盘点单时，是否需要指定逻辑区进行数据过滤")
    public Boolean STOCKTAKE_CREATE_LOGIC_STRATEGY = true;

    @ConfigField(name = "盘点 - 盘点差异是否自动创建调整单", desc = "盘点有异常登记，库存发生异常变动时自动生成库存调整单，如果差异需要上报，则不会自动创建调整单")
    public Boolean STOCKTAKE_CREATE_ADJUSTMENT_AUTO = false;

    @ConfigField(name = "盘点 - 盘点反馈策略", options = StocktakeSyncTypeEnumOption.class, desc = "盘点明细按SKU维度、格口等维度回传" , vif = "STOCKTAKE_CREATE_ADJUSTMENT_AUTO")
    public String STOCKTAKE_DETAIL_CALLBACK_STRATEGY = StocktakeFeedbackTypeEnum.SKU.toString();

    @ConfigField(name = "盘点 - 计算料箱盘点次数策略", desc = "根据配置条件，盘点完成后系统自动增加料箱的盘点次数。输入内容为枚举值，多个用逗号隔开", vif = "STOCKTAKE_CREATE_ADJUSTMENT_AUTO", append = true)
    public QtyAttribute STOCKTAKE_QTY_CONTAINER_STRATEGY = new QtyAttribute(CommonUtil.asList(StocktakeTypeEnum.ORDINARY.toString()), CommonUtil.asList(StocktakeMethodEnum.INFORMED.toString(), StocktakeMethodEnum.BLIND_QTY.toString(), StocktakeMethodEnum.BLIND_SKU_QTY.toString()), StocktakeCreateTypeEnum.RACK.toString() + "," + StocktakeCreateTypeEnum.TOTE.toString());

    @ConfigField(name = "盘点 - 计算料箱盘点次数策略2", desc = "根据配置条件，盘点完成后系统自动增加料箱的盘点次数。输入内容为枚举值，多个用逗号隔开", vif = "STOCKTAKE_CREATE_ADJUSTMENT_AUTO", append = true)
    public List<QtyAttribute> STOCKTAKE_QTY_CONTAINER_STRATEGY2 = CommonUtil.asList(new QtyAttribute(CommonUtil.asList(StocktakeTypeEnum.ORDINARY.toString()), CommonUtil.asList(StocktakeMethodEnum.INFORMED.toString(), StocktakeMethodEnum.BLIND_QTY.toString(), StocktakeMethodEnum.BLIND_SKU_QTY.toString()), StocktakeCreateTypeEnum.RACK.toString() + "," + StocktakeCreateTypeEnum.TOTE.toString()));

    @ConfigField(name = "盘点 - 计算库位盘点次数策略", desc = "根据配置条件，盘点完成后系统自动增加库位的盘点次数。输入内容为枚举值，多个用逗号隔开", vif = "STOCKTAKE_CREATE_ADJUSTMENT_AUTO")
    public QtyAttribute2 STOCKTAKE_QTY_LOCATION_STRATEGY = new QtyAttribute2(CommonUtil.asList(StocktakeTypeEnum.ORDINARY.toString()));

    /**
     * 框架暂时不支持复杂配置的下选选择，先使用逗号隔开，后续再升级
     */
    public static class QtyAttribute {

        @ConfigField(name = "盘点单类型", options = StocktakeTypeEnumOption.class, desc = "普通盘点、差异复盘")
        public List<String> stocktakeType;

        @ConfigField(name = "盘点方式", options = StocktakeMethodEnumOption.class, desc = "明盘、暗盘等方式")
        public List<String> stocktakeMethod;

        @ConfigField(name = "创建类型", options = StocktakeCreateTypeEnumOption.class, desc = "按货架、料箱、商品等创建类型")
        public String createType;

        public QtyAttribute() {
        }

        public QtyAttribute(List<String> stocktakeType, List<String> stocktakeMethod, String createType) {
            this.stocktakeType = stocktakeType;
            this.stocktakeMethod = stocktakeMethod;
            this.createType = createType;
        }
    }


    /**
     * 框架暂时不支持复杂配置的下选选择，先使用逗号隔开，后续再升级
     */
    public static class QtyAttribute2 {

        @ConfigField(name = "盘点单类型", options = StocktakeTypeEnumOption.class, desc = "普通盘点、差异复盘")
        public List<String> stocktakeType;


        public QtyAttribute2() {
        }

        public QtyAttribute2(List<String> stocktakeType) {
            this.stocktakeType = stocktakeType;
        }
    }

    public static class StocktakeTypeEnumOption implements ConfigOption {
        @Override
        public List<Option> getOptions() {
            return Stream.of(StocktakeTypeEnum.values()).map(v -> new Option(v.getDescCN(), v.toString())).collect(Collectors.toList());
        }
    }

    public static class StocktakeMethodEnumOption implements ConfigOption {
        @Override
        public List<Option> getOptions() {
            return Stream.of(StocktakeMethodEnum.values()).map(v -> new Option(v.getDescCN(), v.toString())).collect(Collectors.toList());
        }
    }

    public static class StocktakeCreateTypeEnumOption implements ConfigOption {
        @Override
        public List<Option> getOptions() {
            return Stream.of(StocktakeCreateTypeEnum.values()).map(v -> new Option(v.getDescCN(), v.toString())).collect(Collectors.toList());
        }
    }


    public static class StocktakeSyncTypeEnumOption implements ConfigOption {
        @Override
        public List<Option> getOptions() {
            List<Option> options = new ArrayList<Option>();
            for (StocktakeFeedbackTypeEnum v : StocktakeFeedbackTypeEnum.values()) {
                options.add(new Option(v.getDescCN(), v.toString()));
            }
            return options;
        }
    }

    public static class StocktakeProfitStrategyEnumOption implements ConfigOption {
        @Override
        public List<Option> getOptions() {
            List<Option> options = new ArrayList<Option>();
            for (StocktakeProfitStrategyEnum v : StocktakeProfitStrategyEnum.values()) {
                options.add(new Option(v.getDescCN(), v.toString()));
            }
            return options;
        }
    }

}
