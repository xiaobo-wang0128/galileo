package org.armada.galileo.mybatis.domain;

import lombok.Data;
import lombok.experimental.Accessors;
import org.armada.galileo.model.constant.YesOrNoEnum;

/**
 * @author xiaobo
 * @date 2022/12/6 14:00
 */
@Data
@Accessors(chain = true)
public class BaseDTO {

    /**
     * 主键
     */
    private Long id;

    /**
     * 租户id
     */
    protected Long tenantId;

    /**
     * 创建时间
     */
    private Long gmtCreate;

    /**
     * 更新时间
     */
    private Long gmtModify;

    /**
     * 创建人
     */
    private String creator;

    /**
     * 更新人
     */
    private String modifier;

    /**
     * 删除标记 Y / N
     */
    protected YesOrNoEnum isDelete;

}
