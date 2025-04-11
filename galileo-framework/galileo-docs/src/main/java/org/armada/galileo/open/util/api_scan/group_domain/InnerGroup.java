package org.armada.galileo.open.util.api_scan.group_domain;

import lombok.Data;
import lombok.experimental.Accessors;
import org.armada.galileo.open.util.api_scan.domain.DocItem;

import java.util.List;

/**
 * @author xiaobo
 * @date 2023/2/22 17:41
 */

@Accessors
@Data
public class InnerGroup {
    private int index;
    String name;
    private List<DocItem> children;
}