package org.armada.galileo.i18n_server.dal.bo;

import org.armada.galileo.i18n_server.dal.dto.I18nDictionaryValueDTO;
import org.armada.galileo.i18n_server.dal.entity.I18nDictionaryValue;
import org.armada.galileo.mybatis.bo.BaseBO;

import java.util.List;
import java.util.Set;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author ake
 * @since 2021-12-21
 */

public interface I18nDictionaryValueBO extends BaseBO<I18nDictionaryValue, I18nDictionaryValueDTO> {

    List<Integer> getUnfinishedDictionaryByAppIdAndLocals(Set<String> keys, Integer appId, Integer size);
}
