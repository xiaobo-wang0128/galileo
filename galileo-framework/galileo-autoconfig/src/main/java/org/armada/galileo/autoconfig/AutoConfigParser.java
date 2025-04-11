package org.armada.galileo.autoconfig;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.armada.galileo.autoconfig.annotation.ConfigField;
import org.armada.galileo.autoconfig.annotation.ConfigGroup;
import org.armada.galileo.autoconfig.annotation.ConfigOption;
import org.armada.galileo.autoconfig.annotation.Option;
import org.armada.galileo.autoconfig.form.ATField;
import org.armada.galileo.autoconfig.form.ATFieldOption;
import org.armada.galileo.autoconfig.form.ATFormGroup;
import org.armada.galileo.common.util.CommonUtil;
import org.armada.galileo.common.util.JsonUtil;

import lombok.extern.slf4j.Slf4j;
import org.armada.galileo.exception.BizException;
import org.springframework.util.ReflectionUtils;

/**
 * autoconfig 的解析
 *
 * @author wangxiaobo
 * @date 2021年5月20日
 */
@Slf4j
public class AutoConfigParser {


    public static void preCheck(List<NacosConfig> formValues) {
        if (CommonUtil.isNotEmpty(formValues)) {
            for (NacosConfig formValue : formValues) {
                try {
                    String clsName = formValue.getConfigId();
                    String value = formValue.getConfigValue();

                    Class<?> cls = Class.forName(clsName);

                    ConfigGroup configGroup = cls.getDeclaredAnnotation(ConfigGroup.class);
                    if (configGroup == null) {
                        continue;
                    }

                    if (configGroup.preCheck()) {

                        Method method = ReflectionUtils.findMethod(cls, "preCheck");
                        if (method == null) {
                            log.warn("{} 开启了 preCheck 校验，但没有实现该方法", clsName);
                            continue;
                        }

                        Object obj = JsonUtil.fromJson(value, cls);

                        method.invoke(obj);
                    }
                } catch (Exception e) {
                    log.warn(e.getMessage(), e);
                    throw new RuntimeException(CommonUtil.getDeepExceptionMsg(e));
                }
            }
        }
    }

    public static void afterModify(List<NacosConfig> formValues) {
        if (CommonUtil.isNotEmpty(formValues)) {
            for (NacosConfig formValue : formValues) {
                try {
                    String clsName = formValue.getConfigId();
                    String value = formValue.getConfigValue();

                    Class<?> cls = Class.forName(clsName);

                    ConfigGroup configGroup = cls.getDeclaredAnnotation(ConfigGroup.class);
                    if (configGroup == null) {
                        continue;
                    }

                    if (configGroup.afterModify()) {

                        Method method = ReflectionUtils.findMethod(cls, "afterModify");
                        if (method == null) {
                            log.warn("{} 开启了 afterModify 校验，但没有实现该方法", clsName);
                            continue;
                        }
                        Object obj = JsonUtil.fromJson(value, cls);
                        method.invoke(obj);
                    }
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
    }


    public static ATFormGroup parseForm(Class<?> configClass) {

        ConfigGroup configGroup = configClass.getDeclaredAnnotation(ConfigGroup.class);

        String className = configClass.getName();

//		if ("org.armada.galileo.config.biz.StationConfig$SpaceAttribute".equals(className)) {
//			System.out.println("");
//		}

        String groupName = configGroup != null ? configGroup.group() : "";
        String groupDesc = configGroup != null ? configGroup.desc() : "";
        int groupSort = configGroup != null ? configGroup.sort() : 0;

        List<ATField> fields = new ArrayList<ATField>();

        Field[] fs = configClass.getDeclaredFields();

        for (Field field : fs) {

            String fieldName = field.getName();
            String fieldType = field.getType().getName();

            ConfigField configField = field.getAnnotation(ConfigField.class);

            if (fieldName.startsWith("this$")) {
                continue;
            }
            if (configField == null) {
                log.warn("[autoconfig] ConfigField 注解为空, {}", className + "." + fieldName);
                continue;
            }

            // 字段名
            String name = configField.name();

            // 描述信息
            String desc = configField.desc();

            // 字段编码
            String code = fieldName;

            // 字段类型 text 文本框 textarea 文本域 select 下拉框 date 日期控件 datetime 时间控件 radio 单选
            // checkbox 多选 file 文件上传 email 邮箱 phone 电话 number 数字 address 地址
            // String type = fieldType.substring(fieldType.lastIndexOf(".") + 1);

            // 字段最大长度（type == text/textarea）
            Integer maxLen = configField.maxLen();

            // 类型为 select checkbox 时的选项列表（type == select/checkbox/radio）
            List<ATFieldOption> options = null;

            // 最小值（type == number）
            Integer min = configField.min();

            // 最大值（type == number）
            Integer max = configField.max();

            /// 正则表达式校验
            String regex = configField.regex();

            // 时间格式
            String format = configField.timeFormat();

            String vif = configField.vif();

            boolean append = configField.append();

            boolean readonly = configField.readonly();

            boolean slider = configField.slider();

            // 是否多选
            boolean multiple = false;

            String combinedFields = null;

            int precision = configField.precision();

            List<Option> configOptions = null;
            if (configField.options() != null) {
                try {
                    if (!ConfigOption.class.getName().equals(configField.options().getName())) {
                        configOptions = configField.options().newInstance().getOptions();
                    }
                } catch (Exception e) {
                    log.error(configField.options() + " newInstance 失败, 请检查构造方法:" + e.getMessage(), e);
                }
            }

            String inputType = null;

            // 有可选项， 前端类型一定是 select
            if (configOptions != null && configOptions.size() > 0) {
                options = configOptions.stream().map(e -> {
                    ATFieldOption at = new ATFieldOption();
                    at.setLabel(e.getLabel());
                    at.setRemark(e.getRemark());
                    at.setValue(e.getValue());
                    return at;
                }).collect(Collectors.toList());

                inputType = "select";

                // 配置项定义成 List, 则默认为多选
                if (fieldType.equals("java.util.List")) {
                    multiple = true;
                } else {
                    multiple = false;
                }
            }
            // 判断没有可选项的场景
            else {

                // 基础类型
                if (fieldType.startsWith("java.lang")) {

                    if (fieldType.endsWith("Boolean")) {
                        inputType = "boolean";
                    } else if (fieldType.endsWith("Byte")) {
                        inputType = "number";
                    } else if (fieldType.endsWith("Double")) {
                        inputType = "number";
                    } else if (fieldType.endsWith("Float")) {
                        inputType = "number";
                    } else if (fieldType.endsWith("Integer")) {
                        inputType = "number";
                    } else if (fieldType.endsWith("Long")) {
                        inputType = "number";
                    } else if (fieldType.endsWith("Short")) {
                        inputType = "number";
                    } else if (fieldType.endsWith("String")) {
                        if (configField.singleLine()) {
                            inputType = "text";
                        } else {
                            inputType = "textarea";
                        }
                    }

                }
                // List 类型
                else if (fieldType.equals("java.util.List")) {

                    // 只要定义成 list ，即为多选
                    multiple = true;

                    String genericType = field.getGenericType().getTypeName();

                    if (genericType.indexOf("<") == -1) {
                        throw new RuntimeException("[autoconfig] 配置项`" + className + "." + field.getName() + "`必须指定泛型");
                    }

                    String innerType = genericType.substring(genericType.indexOf("<") + 1, genericType.indexOf(">"));

                    // 没有可选项，但却是 list 类型， 前端则定义为可 编辑的 select
                    if (isBaseType(innerType)) {
                        inputType = "select";
                    }
                    // 组合表单
                    else {
                        inputType = "combined";

                        // 加载子配置项
                        try {

                            Class<?> innerClass = Class.forName(innerType);

                            ATFormGroup innserFormGroup = AutoConfigParser.parseForm(innerClass);

                            combinedFields = JsonUtil.toJson(innserFormGroup.getFields());

                        } catch (Exception e) {
                            log.error(e.getMessage(), e);
                        }
                    }
                } else {

                    inputType = "combined";

                    // 加载子配置项
                    try {

                        Class<?> innerClass = field.getType();

                        ATFormGroup innserFormGroup = AutoConfigParser.parseForm(innerClass);

                        combinedFields = JsonUtil.toJson(innserFormGroup.getFields());

                    } catch (Exception e) {

                        log.error(e.getMessage(), e);

                    }

                }
            }

            ATField at = new ATField();
            at.setName(name);
            at.setDesc(desc);
            at.setCode(code);
            at.setType(inputType);
            at.setMaxLen(maxLen);
            at.setOptions(options);
            at.setMin(min);
            at.setMax(max);
            at.setRegex(regex);
            at.setFormat(format);
            at.setMultiple(multiple);
            at.setPrecision(precision);
            at.setCombinedFields(combinedFields);
            at.setReadonly(readonly);
            at.setSlider(slider);
            at.setVif(vif);
            at.setAppend(append);
            fields.add(at);
        }

        ATFormGroup atFormGroup = new ATFormGroup();

        atFormGroup.setClassName(className);
        atFormGroup.setGroup(groupName);
        atFormGroup.setDesc(groupDesc);
        atFormGroup.setSort(groupSort);
        atFormGroup.setFields(fields);

        return atFormGroup;

    }

    private static boolean isBaseType(String type) {
        if (type.endsWith("Boolean")) {
            return true;
        } else if (type.endsWith("Byte")) {
            return true;
        } else if (type.endsWith("Double")) {
            return true;
        } else if (type.endsWith("Float")) {
            return true;
        } else if (type.endsWith("Integer")) {
            return true;
        } else if (type.endsWith("Long")) {
            return true;
        } else if (type.endsWith("Short")) {
            return true;
        } else if (type.endsWith("String")) {
            return true;
        }
        return false;
    }

}
