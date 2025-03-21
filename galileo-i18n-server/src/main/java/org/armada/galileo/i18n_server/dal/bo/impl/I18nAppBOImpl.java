package org.armada.galileo.i18n_server.dal.bo.impl;


import lombok.extern.slf4j.Slf4j;
import org.armada.galileo.i18n_server.dal.bo.I18nAppBO;
import org.armada.galileo.i18n_server.dal.dto.I18nAppDTO;
import org.armada.galileo.i18n_server.dal.entity.I18nApp;
import org.armada.galileo.i18n_server.dal.mapper.I18nAppMapper;
import org.armada.galileo.i18n_server.dal.transfer.I18nAppTransfer;
import org.armada.galileo.mybatis.bo.impl.BaseBOImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author ake
 * @since 2021-12-21
 */
@Service
@Slf4j
public class I18nAppBOImpl extends BaseBOImpl<I18nApp, I18nAppDTO, I18nAppMapper, I18nAppTransfer> implements I18nAppBO {


}


