package org.armada.galileo.open.util.i18n_scan;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;

/**
 * @author xiaobo
 * @date 2023/1/3 18:13
 */
@Data
@Accessors(chain = true)
public class EnumItem {

    private String typeName;

    private List<String> keys;

    private Map<String, String> defaultDesc;

    private EnumItemType type;


    public static enum EnumItemType{

        I18nDictionary,

        I18nError

    }
}
