package org.armada.galileo.open.util.api_scan.domain;

import lombok.Data;

import java.util.List;

@Data
public class DocEntity {

    private String group;

    private Integer groupSort = 0;

    private List<DocItem> docList;

}
