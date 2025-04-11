package org.armada.galileo.open.util.i18n_scan;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Map;

/**
 * @author ake
 * @since 2021-12-21
 */
@Data
@Accessors(chain = true)
public class I18nDictionaryKeyUpload {

    /**
     * 词条键值
     */
    private String dictionaryKey;

    private Map<String, String> dictValueMap;

}
