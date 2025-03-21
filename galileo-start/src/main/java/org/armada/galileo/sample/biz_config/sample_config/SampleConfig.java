package org.armada.galileo.sample.biz_config.sample_config;


import java.util.ArrayList;
import java.util.List;

import org.armada.galileo.autoconfig.AutoConfigGalileo;
import org.armada.galileo.autoconfig.annotation.ConfigField;
import org.armada.galileo.autoconfig.annotation.ConfigGroup;
import org.armada.galileo.autoconfig.annotation.ConfigOption;
import org.armada.galileo.autoconfig.annotation.Option;
import org.armada.galileo.common.util.CommonUtil;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Configuration
@Data
@ConfigGroup(group = "测试示例（仅用于演示，不在些配置类中添加与业务有关的配置）", sort = 999)
public class SampleConfig implements AutoConfigGalileo {

    @ConfigField(name = "单行文本")
    private String simpleText = "这是一段单行文本";

    @ConfigField(name = "文本域", singleLine = false, maxLen = 100)
    private String textArea = "这是一段多行文本";

    @ConfigField(name = "带选项的配置", options = WorkMdesOptions.class)
    private String workModes = "1";

    @ConfigField(name = "带选项的配置（多选）", options = WorkMdesOptions.class)
    private List<String> workModeLists = CommonUtil.asList("1", "2");

    @ConfigField(name = "bool类型配置项")
    private Boolean empty = true;

    @ConfigField(name = "int型配置项")
    private Integer maxPickNum = 20;

    @ConfigField(name = "int-slider", slider = true, max = 10, min = 1)
    private Integer maxPriceddd = 2;

    @ConfigField(name = "double型配置项",precision = 2)
    private Double maxPrice = 20.12D;


    @ConfigField(name = "组合式配置项（List形式）料箱规格定义（单位:cm）", desc = "按客户要求定义料箱的规格信息", append = true)
    private List<SpaceAttribute> spaceAttributes = CommonUtil.asList(
            // min
            new SpaceAttribute("min", "10-20", "10-20", "10-20"),
            // mid
            new SpaceAttribute("mid", "15-30", "15-30", "15-30"),
            // max
            new SpaceAttribute("max", "30-50", "30-50", "30-50"));

    @ConfigField(name = "组合式配置项（单对象）（单位:cm）", desc = "按客户要求定义料箱的规格信息")
    private SpaceAttribute spaceAttribute2 = new SpaceAttribute("min", "10-20", "10-20", "10-20");

    class SpaceAttribute {

        /**
         * 该字段不希望用户修改，可标记成 readonly
         */
        @ConfigField(name = "标识", desc = "代码中会用到，请勿修改", readonly = true)
        private String type;

        @ConfigField(name = "长度范围")
        private String length;

        @ConfigField(name = "宽度范围")
        private String width;

        @ConfigField(name = "高度范围")
        private String height;

        public SpaceAttribute() {
        }

        public SpaceAttribute(String type, String length, String width, String height) {
            this.type = type;
            this.length = length;
            this.width = width;
            this.height = height;
        }
    }

    public static class WorkMdesOptions implements ConfigOption {

        @Override
        public List<Option> getOptions() {
            List<Option> options = new ArrayList<Option>();
            options.add(new Option("出库", "1"));
            options.add(new Option("入库", "2"));
            options.add(new Option("盘点", "3"));
            options.add(new Option("理库", "4"));
            return options;
        }
    }

    ;
}
