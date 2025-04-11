package org.armada.galileo.i18n_server.dal.bo.impl;


import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.extern.slf4j.Slf4j;
import org.armada.galileo.i18n_server.dal.bo.I18nDictionaryKeyBO;
import org.armada.galileo.i18n_server.dal.dto.I18nDictionaryKeyDTO;
import org.armada.galileo.i18n_server.dal.entity.I18nDictionaryKey;
import org.armada.galileo.i18n_server.dal.mapper.I18nDictionaryKeyMapper;
import org.armada.galileo.i18n_server.dal.transfer.I18nDictionaryKeyTransfer;
import org.armada.galileo.mybatis.bo.impl.BaseBOImpl;
import org.springframework.stereotype.Service;

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
public class I18nDictionaryKeyBOImpl extends BaseBOImpl<I18nDictionaryKey, I18nDictionaryKeyDTO, I18nDictionaryKeyMapper, I18nDictionaryKeyTransfer> implements I18nDictionaryKeyBO {


    @Override
    public I18nDictionaryKeyDTO selectI18nDictionaryByKey(String resKey) {
        return convertToDto(getBaseMapper().selectOne(Wrappers.<I18nDictionaryKey>lambdaQuery().eq(I18nDictionaryKey::getDictionaryKey, resKey)));
    }
}


