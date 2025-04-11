package org.armada.galileo.i18n_server.dal.bo.impl;


import lombok.extern.slf4j.Slf4j;
import org.armada.galileo.i18n_server.dal.bo.I18nBranchKeysBO;
import org.armada.galileo.i18n_server.dal.dto.I18nBranchKeysDTO;
import org.armada.galileo.i18n_server.dal.entity.I18nBranchKeys;
import org.armada.galileo.i18n_server.dal.mapper.I18nBranchKeysMapper;
import org.armada.galileo.i18n_server.dal.transfer.I18nBranchKeysTransfer;
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
public class I18nBranchKeysBOImpl extends BaseBOImpl<I18nBranchKeys, I18nBranchKeysDTO, I18nBranchKeysMapper, I18nBranchKeysTransfer> implements I18nBranchKeysBO {



}


