package org.armada.galileo.i18n_server.dal.transfer;


import org.armada.galileo.i18n_server.dal.dto.I18nBranchKeysDTO;
import org.armada.galileo.i18n_server.dal.entity.I18nBranchKeys;
import org.armada.galileo.mybatis.bo.MapstructConvertor;
import org.mapstruct.Mapper;

import static org.mapstruct.NullValueCheckStrategy.ALWAYS;
import static org.mapstruct.NullValueMappingStrategy.RETURN_NULL;

/**
 * @author ake
 * @since 2021-12-21
 */
@Mapper(componentModel = "spring", nullValueCheckStrategy = ALWAYS, nullValueMappingStrategy = RETURN_NULL)
public interface I18nBranchKeysTransfer extends MapstructConvertor<I18nBranchKeys, I18nBranchKeysDTO> {
}


