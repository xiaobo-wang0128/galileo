package com.haigui.common.pdf.constant;

/**
 * @Author: xuhui
 * @Description:
 * @Date: 2020/2/22 上午12:56
 */
public enum PaginationAlignMode {
     // 居中
     center,
     // 居左
     left,
     // 居右
     right,
     // 单页居左，双页居右
     side,
    ;

    public static PaginationAlignMode parse(String mode) {
        for (PaginationAlignMode value : values()) {
            if (value.name().equals(mode)) {
                return value;
            }
        }
        throw new IllegalArgumentException("mode error");
    }
}

