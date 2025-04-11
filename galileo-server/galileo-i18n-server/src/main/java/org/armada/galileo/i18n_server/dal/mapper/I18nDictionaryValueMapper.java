package org.armada.galileo.i18n_server.dal.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.armada.galileo.i18n_server.dal.dto.DictionaryDTO;
import org.armada.galileo.i18n_server.dal.entity.I18nDictionaryValue;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author ake
 * @since 2021-12-21
 */
public interface I18nDictionaryValueMapper extends BaseMapper<I18nDictionaryValue> {

    List<DictionaryDTO> getValueByAppAndlocale(@Param("appId") Integer appId, @Param("locale") String locale);

    List<Map<String,Object>> getUnfinishedDictionaryByAppIdAndLocals(@Param("keys") Set<String> keys, @Param("appId") Integer appId, @Param("size") Integer size);

}
