package org.armada.galileo.open.dal.es_mapper.impl;

import org.armada.galileo.es.mapper.EsBaseMapper;
import org.armada.galileo.es.mapper.EsBaseMapperImpl;
import org.armada.galileo.open.dal.entity.OpenRequestMessage;
import org.armada.galileo.open.dal.es_mapper.OpenRequestMessageMapper;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 开放平台接口请求日志 Mapper 接口
 * </p>
 *
 * @author
 * @since 2023-02-03
 */
@Service
public class OpenRequestMessageMapperImpl extends EsBaseMapperImpl<OpenRequestMessage> implements OpenRequestMessageMapper {
}
