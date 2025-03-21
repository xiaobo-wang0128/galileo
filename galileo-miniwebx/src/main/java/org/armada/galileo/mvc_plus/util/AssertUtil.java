package org.armada.galileo.mvc_plus.util;

import java.util.Date;
import java.util.Map;

import org.armada.galileo.common.util.CommonUtil;
import org.armada.galileo.common.util.IDCardUtil;
import org.armada.galileo.exception.BizException;


/**
 * 输入验证类，该类的验证中不符合规则就抛异常，用于强制性约束
 *
 * @author wang xiaobo
 */
public class AssertUtil {

    /**
     * 判定输入不为空，如为空则抛异常
     *
     * @param input
     * @param fieldName 字段名
     */
    public static void isNotNull(Object input, String fieldName) {
        if (input instanceof String) {
            if (input == null || input.toString().matches("\\s*")) {
                throw new BizException(fieldName + "不允许为空");
            }
        } else {
            if (input == null) {
                throw new BizException(fieldName + "不允许为空");
            }
        }
    }

    /**
     * 判定输入不为空，如为空则抛异常
     *
     * @param input
     * @param fieldName 字段名
     */
    public static void isNotNull(Object input, String fieldName, Map<String, Object> errorData) {
        if (input instanceof String) {
            if (input == null || input.toString().matches("\\s*")) {
                throw new BizException(fieldName + "不能为空", errorData);
            }
        } else {
            if (input == null) {
                throw new BizException(fieldName + "不能为空", errorData);
            }
        }
    }

    /**
     * 判断输入必须为数字，如不是数字则抛异常
     *
     * @param input
     * @param fieldName 字段名
     */
    public static void isNumber(String input, String fieldName) {
        try {
            Double.valueOf(input);
        } catch (Exception e) {
            throw new BizException(fieldName + "必须为数字");
        }
    }

    /**
     * 输入荐必须为数字
     *
     * @param input
     * @param fieldName
     * @param errorData
     */
    public static void isNumber(String input, String fieldName, Map<String, Object> errorData) {
        try {
            Double.valueOf(input);
        } catch (Exception e) {
            throw new BizException(fieldName + "必须为数字", errorData);
        }
    }

    /**
     * 判断输入必须为整型，否则则抛异常
     *
     * @param input
     * @param fieldName 字段名
     */
    public static void isInteger(String input, String fieldName) {
        try {
            Integer.valueOf(input);
        } catch (Exception e) {
            throw new BizException(fieldName + "必须为整数");
        }
    }

    /**
     * 第一个输入项 必须 大于第二个输入项，否则抛异常
     *
     * @param num1
     * @param num2
     * @param fieldName1 字段1
     * @param fieldName2 字段2
     */
    public static void greaterThan(Number num1, Number num2, String fieldName1, String fieldName2) {
        if (num1 == null || num2 == null) {
            throw new BizException(fieldName1 + "必须大于" + fieldName2);
        }

        if (num1.doubleValue() <= num2.doubleValue()) {
            throw new BizException(fieldName1 + "必须大于" + fieldName2);
        }
    }

    /**
     * 第一个输入项 必须 大于等于第二个输入项，否则抛异常
     *
     * @param num1
     * @param num2
     * @param fieldName1 字段1
     * @param fieldName2 字段2
     */
    public static void greaterThanOrEqual(Number num1, Number num2, String fieldName1, String fieldName2) {
        if (num1 == null || num2 == null) {
            throw new BizException(fieldName1 + "必须大于等于" + fieldName2);
        }

        if (num1.doubleValue() < num2.doubleValue()) {
            throw new BizException(fieldName1 + "必须大于等于" + fieldName2);
        }
    }

    /**
     * 第一个输入项 必须 大于等于第二个输入项，否则抛异常
     *
     * @param date1
     * @param date2
     * @param fieldName1 字段1
     * @param fieldName2 字段2
     */
    public static void greaterThanOrEqual(Date date1, Date date2, String fieldName1, String fieldName2) {
        if (date1 == null || date2 == null) {
            throw new BizException(fieldName1 + "必须大于等于" + fieldName2);
        }

        if (date1.getTime() < date2.getTime()) {
            throw new BizException(fieldName1 + "必须大于等于" + fieldName2);
        }
    }

    /**
     * 是否为手机号
     *
     * @param mobile
     */
    public static void isMobile(String mobile) {
        if (!CommonUtil.isMobileNO(mobile)) {
            throw new BizException("手机号格式错误");
        }
    }

    /**
     * 判断用户输入是否为身份证号
     *
     * @param identityCard
     */
    public static void isIdentityCard(String identityCard) {
        if (!IDCardUtil.isIDCard(identityCard)) {
            throw new BizException("身份证号格式不正确");
        }
    }

    /**
     * 判断2个输入项是否相待.例：判断用户输入的密码、确认密码是否相等
     *
     * @param str1 输入项1
     * @param str2 输入项2
     * @param err  报错信息
     */
    public static void equals(Object str1, Object str2, String err) {
        if (str1 == null || str2 == null) {
            throw new BizException(err);
        }
        if (!str1.equals(str2)) {
            throw new BizException(err);
        }
    }

    /**
     * 输入长度限制
     *
     * @param input  输入值
     * @param field  参数名
     * @param maxLen 最大长度
     */
    public static void overMaxLength(String input, String field, int maxLen) {
        if (input == null) {
            return;
        }
        if (input.length() > maxLen) {
            throw new BizException(field + "不能超过" + maxLen + "个字符");
        }
    }

    public static void isEmail(String input) {
        if (!CommonUtil.isEmail(input)) {
            throw new BizException("邮箱格式不正确");
        }
    }

    public static void isLong(String input) {
        try {
            Long.valueOf(input);
        } catch (Exception e) {
            throw new BizException("input " + input + " is not long");
        }

    }

    public static void isNotNull(Object input) {
        if (input instanceof String) {
            if (input == null || input.toString().matches("\\s*")) {
                throw new BizException("不允许为空");
            }
        } else {
            if (input == null) {
                throw new BizException("不允许为空");
            }
        }
    }

    public static void isNotEmpty(Object input,   String fieldName) {
        if(CommonUtil.isEmpty(input)){
            throw new BizException(fieldName + "不允许为空");
        }
    }

}
