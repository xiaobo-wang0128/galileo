package com.haigui.common.constant;

/**
 * @Author: xuhui
 * @Description:
 * @Date: 2020/4/7 下午4:43
 */
public interface AccusedType {

    /**
     * 被告类型，自然人，法人
     */
    String ACCUSED_NORMAL = "自然人";
    String ACCUSED_LEGAL = "法人";
    /**
     * 新增第三人类型
     */
    String ACCUSED_NORMAL_THIRD = "第三人（自然人）";
    String ACCUSED_LEGAL_THIRD = "第三人（法人）";

    static boolean isNormal(String accusedType) {
        return ACCUSED_NORMAL.equals(accusedType) || ACCUSED_NORMAL_THIRD.equals(accusedType);
    }

    static boolean isLegal(String accusedType) {
        return !isNormal(accusedType);
    }

}
