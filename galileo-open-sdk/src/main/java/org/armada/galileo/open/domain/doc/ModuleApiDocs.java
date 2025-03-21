package org.armada.galileo.open.domain.doc;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.armada.galileo.open.util.api_scan.domain.DocItem;

import java.io.Serializable;
import java.util.List;

/**
 * @author xiaobo
 * @date 2021/11/2 9:51 下午
 */

@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class ModuleApiDocs implements Serializable {

    /**
     * 子系统名称
     */
    private String name = "test";

    private List<Group> groups;

    private int index;

    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Group{
        private int index;
        private String name;
        private List<DocItem> children;
    }
}
