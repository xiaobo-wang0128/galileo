package org.armada.galileo.i18n_server.dal.bo.impl;


import cn.hutool.core.collection.CollUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.armada.galileo.i18n_server.dal.bo.I18nDictionaryValueBO;
import org.armada.galileo.i18n_server.dal.dto.I18nDictionaryValueDTO;
import org.armada.galileo.i18n_server.dal.entity.I18nDictionaryValue;
import org.armada.galileo.i18n_server.dal.mapper.I18nDictionaryValueMapper;
import org.armada.galileo.i18n_server.dal.transfer.I18nDictionaryValueTransfer;
import org.armada.galileo.mybatis.bo.impl.BaseBOImpl;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author ake
 * @since 2021-12-21
 */
@Service
@Slf4j
public class I18nDictionaryValueBOImpl extends BaseBOImpl<I18nDictionaryValue, I18nDictionaryValueDTO, I18nDictionaryValueMapper, I18nDictionaryValueTransfer> implements I18nDictionaryValueBO {


    @Override
    public List<Integer> getUnfinishedDictionaryByAppIdAndLocals(Set<String> keys, Integer appId, Integer size) {
        List<Integer> dictionaryKeyIds = Lists.newArrayList();
        List<Map<String, Object>> maps = mapper.getUnfinishedDictionaryByAppIdAndLocals(keys,appId, size);
        if (CollUtil.isNotEmpty(maps)) {
            for (Map<String, Object> map : maps) {
                Object dictionaryKeyId = map.get("dictionaryKeyId");
                if (dictionaryKeyId != null)
                    dictionaryKeyIds.add((Integer) dictionaryKeyId);
            }
        }
        return dictionaryKeyIds;
    }
}


