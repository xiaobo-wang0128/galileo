package org.armada.galileo.model.constant;

import java.util.List;

/**
 * @author xiaobo
 * @date 2022/12/25 16:51
 */
public interface I18nDictionary {

    String getDesc();

    default String toI18nValue() {
        return null;
    }

    default List<String> getOptionName() {
        return null;
    }

    ;

}

