package org.armada.galileo.open.util.api_scan.domain;


import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;


@Data
@Accessors(chain = true)
public class PageResult<T> {

    private List<T> list;

    // 当前页
    private Integer pageIndex;

    // 每页显示数量
    private Integer pageSize;

    // 总页数
    private Integer totalPage;

    // 总记录数
    private Integer totalSize;

    // 是否有下一页
    private Boolean hasNext;

    // 是否有上一页
    private Boolean hasPre;

}

