package org.armada.galileo.i18n_server.dal.bo;

import org.armada.galileo.i18n_server.dal.dto.I18nDictionaryKeyDTO;
import org.armada.galileo.i18n_server.dal.entity.I18nDictionaryKey;
import org.armada.galileo.mybatis.bo.BaseBO;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author ake
 * @since 2021-12-21
 */

public interface I18nDictionaryKeyBO extends BaseBO<I18nDictionaryKey, I18nDictionaryKeyDTO> {

    I18nDictionaryKeyDTO selectI18nDictionaryByKey(String resKey);
}
