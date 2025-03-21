package org.armada.galileo.open.util.api_scan.group_domain;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author xiaobo
 * @date 2023/2/22 17:41
 */
@Accessors
@Data
public class MainGroup {
    private int index;
    String name;
    String appName;
    String urlHead;
    private List<InnerGroup> groups;
}
