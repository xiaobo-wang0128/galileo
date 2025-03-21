package org.armada.galileo.open.util.api_scan.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

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
public class DocGroup implements Serializable {

    private String group;

    private List<DocItem> docList;
}
