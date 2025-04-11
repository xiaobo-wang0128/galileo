package org.armada.galileo.i18n_server.dal.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.armada.galileo.i18n_server.dal.dto.I18nDictionaryKeyDTO;
import org.armada.galileo.i18n_server.dal.entity.I18nDictionaryKey;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author ake
 * @since 2021-12-21
 */
public interface I18nDictionaryKeyMapper extends BaseMapper<I18nDictionaryKey> {


    I18nDictionaryKeyDTO getDictionaryByAppCodeAndDictionaryKey(@Param("appId") Integer appId, @Param("dictionaryKey") String dictionaryKey);
}
