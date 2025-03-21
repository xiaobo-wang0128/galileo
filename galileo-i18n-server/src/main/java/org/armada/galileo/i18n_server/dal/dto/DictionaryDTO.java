package org.armada.galileo.i18n_server.dal.dto;

import lombok.Data;

@Data
public class DictionaryDTO {
    /**
     * 词条键值
     */
    private String dictionaryKey;
    /**
     * 词条对应值
     */
    private String dictionaryValue;
}
