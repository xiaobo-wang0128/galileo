package org.armada.galileo.mybatis.sequence;

import lombok.Data;
import lombok.experimental.Accessors;
import org.armada.galileo.mybatis.annotation.TableColumn;
import org.armada.galileo.mybatis.annotation.TableIndex;
import org.armada.galileo.mybatis.annotation.TableIndexKey;

import java.io.Serializable;

/**
 * @author xiaobo
 * @date 2022/12/18 18:07
 */
@Data
@Accessors
@TableIndex({
        @TableIndexKey(value = {"id"}, unique = true),
})
public class SequenceNo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键id
     */
    @TableColumn(comment = "租户id", notNull = true)
    private String id;

    /**
     * 编号头
     */
    @TableColumn(comment = "租户id", notNull = true)
    private String head;

    /**
     * 日期
     */
    @TableColumn(comment = "租户id", notNull = true)
    private String day;

    /**
     * 当前序号
     */
    @TableColumn(comment = "租户id", notNull = true)
    private Integer currentIndex;

    /**
     * 当前编号
     */
    @TableColumn(comment = "租户id", notNull = true)
    private String currentNo;

}
