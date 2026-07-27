package org.armada.galileo.autoconfig.util;

import lombok.Data;
import lombok.experimental.Accessors;
import org.armada.galileo.mybatis.annotation.TableColumn;
import org.armada.galileo.mybatis.annotation.TableIndex;
import org.armada.galileo.mybatis.annotation.TableIndexKey;

/**
 * 租房配置信息表
 * @author xiaobo
 * @date 2024/1/12 15:10
 */
@Data
@Accessors
@TableIndex({
        @TableIndexKey(value = {"tenant_id", "tenant_type", "config_class"}, unique = true),
})
public class AutoConfigEntity {

    /**
     * 租户id
     */
    @TableColumn(comment = "租户id", notNull = true)
    private Long tenantId;

    /**
     * 租户类型
     */
    @TableColumn(comment = "租户类型", len = 16, notNull = true)
    private TenantTypeEnum tenantType;

    /**
     * 配置类
     */
    @TableColumn(comment = "配置类", len = 256, notNull = true)
    private String configClass;

    /**
     * 配置值
     */
    @TableColumn(comment = "配置值", type="text", notNull = true)
    private String configValue;

    /**
     * 更新时间
     */
    @TableColumn(comment = "更新时间", notNull = true)
    private Long updateTime;

}
