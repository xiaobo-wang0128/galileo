package org.armada.galileo.i18n_server.dal.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author ake
 * @date 2022/4/21 15:09
 */

@Getter
@AllArgsConstructor
public enum BranchTypeEnum {
    /**
     * IWMS-前端
     */
    IWMS_FRONT("IWMS_FRONT", "iwms", "IWMS前端分支"),
    /**
     * IWMS-后端
     */
    IWMS_BACK("IWMS_BACK", "iwms", "IWMS后端分支");

    private final String code;

    private final String appCode;

    private final String name;
}
