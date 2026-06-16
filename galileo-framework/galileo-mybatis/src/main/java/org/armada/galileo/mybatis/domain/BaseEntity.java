package org.armada.galileo.mybatis.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.experimental.Accessors;
import org.armada.galileo.es.annotation.EsIndex;
import org.armada.galileo.model.constant.YesOrNoEnum;
import org.armada.galileo.mybatis.annotation.TableColumn;
import org.armada.galileo.mybatis.annotation.TableIndex;
import org.armada.galileo.mybatis.annotation.TableIndexKey;

@Data
@Accessors(chain = true)
@TableIndex({
        @TableIndexKey(value = "tenant_id"),
        @TableIndexKey(value = "gmt_create"),
        @TableIndexKey(value = "gmt_modify"),
        @TableIndexKey(value = "is_delete")
})
public class BaseEntity {

    /**
     * 主键
     */
    @TableId(type = IdType.ASSIGN_ID)
    @TableColumn(comment = "主键", notNull = true)
    private Long id;

    /**
     * 租户id
     */
    @EsIndex
    @TableColumn(comment = "租户id", notNull = true)
    private Long tenantId;

    /**
     * 创建时间
     */
    @EsIndex
    @TableColumn(comment = "创建时间", notNull = true)
    private Long gmtCreate;

    /**
     * 更新时间
     */
    @EsIndex
    @TableColumn(comment = "更新时间", notNull = true)
    private Long gmtModify;

    /**
     * 创建人
     */
    @TableColumn(comment = "创建人", len = 32)
    private String creator;

    /**
     * 更新人
     */
    @TableColumn(comment = "更新人", len = 32)
    private String modifier;

    /**
     * 删除标记 Y / N
     */
    @EsIndex
    @TableColumn(comment = "删除标记", notNull = true, defaultValue = "N")
    private YesOrNoEnum isDelete;

}
