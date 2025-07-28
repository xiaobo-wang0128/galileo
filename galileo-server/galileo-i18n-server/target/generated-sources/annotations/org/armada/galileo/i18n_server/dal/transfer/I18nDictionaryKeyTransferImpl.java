package org.armada.galileo.i18n_server.dal.transfer;

import javax.annotation.processing.Generated;
import org.armada.galileo.i18n_server.dal.dto.I18nDictionaryKeyDTO;
import org.armada.galileo.i18n_server.dal.entity.I18nDictionaryKey;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-06-03T17:08:40+0800",
    comments = "version: 1.5.0.Beta2, compiler: javac, environment: Java 11.0.27 (Azul Systems, Inc.)"
)
@Component
public class I18nDictionaryKeyTransferImpl implements I18nDictionaryKeyTransfer {

    @Override
    public I18nDictionaryKey toDO(I18nDictionaryKeyDTO arg0) {
        if ( arg0 == null ) {
            return null;
        }

        I18nDictionaryKey i18nDictionaryKey = new I18nDictionaryKey();

        if ( arg0.getId() != null ) {
            i18nDictionaryKey.setId( arg0.getId() );
        }
        if ( arg0.getCreateTime() != null ) {
            i18nDictionaryKey.setCreateTime( arg0.getCreateTime() );
        }
        if ( arg0.getUpdateTime() != null ) {
            i18nDictionaryKey.setUpdateTime( arg0.getUpdateTime() );
        }
        if ( arg0.getCreateUser() != null ) {
            i18nDictionaryKey.setCreateUser( arg0.getCreateUser() );
        }
        if ( arg0.getUpdateUser() != null ) {
            i18nDictionaryKey.setUpdateUser( arg0.getUpdateUser() );
        }
        if ( arg0.getCreateUserId() != null ) {
            i18nDictionaryKey.setCreateUserId( arg0.getCreateUserId() );
        }
        if ( arg0.getUpdateUserId() != null ) {
            i18nDictionaryKey.setUpdateUserId( arg0.getUpdateUserId() );
        }
        if ( arg0.getStatus() != null ) {
            i18nDictionaryKey.setStatus( arg0.getStatus() );
        }
        if ( arg0.getAppId() != null ) {
            i18nDictionaryKey.setAppId( arg0.getAppId() );
        }
        if ( arg0.getAppCode() != null ) {
            i18nDictionaryKey.setAppCode( arg0.getAppCode() );
        }
        if ( arg0.getDictionaryKey() != null ) {
            i18nDictionaryKey.setDictionaryKey( arg0.getDictionaryKey() );
        }

        return i18nDictionaryKey;
    }

    @Override
    public I18nDictionaryKeyDTO toDTO(I18nDictionaryKey arg0) {
        if ( arg0 == null ) {
            return null;
        }

        I18nDictionaryKeyDTO i18nDictionaryKeyDTO = new I18nDictionaryKeyDTO();

        if ( arg0.getId() != null ) {
            i18nDictionaryKeyDTO.setId( arg0.getId() );
        }
        if ( arg0.getAppId() != null ) {
            i18nDictionaryKeyDTO.setAppId( arg0.getAppId() );
        }
        if ( arg0.getAppCode() != null ) {
            i18nDictionaryKeyDTO.setAppCode( arg0.getAppCode() );
        }
        if ( arg0.getDictionaryKey() != null ) {
            i18nDictionaryKeyDTO.setDictionaryKey( arg0.getDictionaryKey() );
        }
        if ( arg0.getCreateTime() != null ) {
            i18nDictionaryKeyDTO.setCreateTime( arg0.getCreateTime() );
        }
        if ( arg0.getUpdateTime() != null ) {
            i18nDictionaryKeyDTO.setUpdateTime( arg0.getUpdateTime() );
        }
        if ( arg0.getCreateUser() != null ) {
            i18nDictionaryKeyDTO.setCreateUser( arg0.getCreateUser() );
        }
        if ( arg0.getUpdateUser() != null ) {
            i18nDictionaryKeyDTO.setUpdateUser( arg0.getUpdateUser() );
        }
        if ( arg0.getCreateUserId() != null ) {
            i18nDictionaryKeyDTO.setCreateUserId( arg0.getCreateUserId() );
        }
        if ( arg0.getUpdateUserId() != null ) {
            i18nDictionaryKeyDTO.setUpdateUserId( arg0.getUpdateUserId() );
        }
        if ( arg0.getStatus() != null ) {
            i18nDictionaryKeyDTO.setStatus( arg0.getStatus() );
        }

        return i18nDictionaryKeyDTO;
    }
}
