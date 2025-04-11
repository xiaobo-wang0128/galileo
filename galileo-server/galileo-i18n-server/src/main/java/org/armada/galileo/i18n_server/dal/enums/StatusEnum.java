package org.armada.galileo.i18n_server.dal.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;
/**
 * @author ake
 * @since 2021-12-21
 */
@Getter
@AllArgsConstructor
public enum StatusEnum {
    /**
     * 启用
     */
    ENABLE("enable"),

    /**
     * 禁用
     */
    DISABLE("disable");

    @EnumValue
    private final String value;
}
