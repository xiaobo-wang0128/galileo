package org.armada.galileo.i18n_server.dal.transfer;

import javax.annotation.processing.Generated;
import org.armada.galileo.i18n_server.dal.dto.I18nDictionaryValueDTO;
import org.armada.galileo.i18n_server.dal.entity.I18nDictionaryValue;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-06-03T17:08:40+0800",
    comments = "version: 1.5.0.Beta2, compiler: javac, environment: Java 11.0.27 (Azul Systems, Inc.)"
)
@Component
public class I18nDictionaryValueTransferImpl implements I18nDictionaryValueTransfer {

    @Override
    public I18nDictionaryValue toDO(I18nDictionaryValueDTO arg0) {
        if ( arg0 == null ) {
            return null;
        }

        I18nDictionaryValue i18nDictionaryValue = new I18nDictionaryValue();

        if ( arg0.getId() != null ) {
            i18nDictionaryValue.setId( arg0.getId() );
        }
        if ( arg0.getCreateTime() != null ) {
            i18nDictionaryValue.setCreateTime( arg0.getCreateTime() );
        }
        if ( arg0.getUpdateTime() != null ) {
            i18nDictionaryValue.setUpdateTime( arg0.getUpdateTime() );
        }
        if ( arg0.getCreateUser() != null ) {
            i18nDictionaryValue.setCreateUser( arg0.getCreateUser() );
        }
        if ( arg0.getUpdateUser() != null ) {
            i18nDictionaryValue.setUpdateUser( arg0.getUpdateUser() );
        }
        if ( arg0.getCreateUserId() != null ) {
            i18nDictionaryValue.setCreateUserId( arg0.getCreateUserId() );
        }
        if ( arg0.getUpdateUserId() != null ) {
            i18nDictionaryValue.setUpdateUserId( arg0.getUpdateUserId() );
        }
        if ( arg0.getStatus() != null ) {
            i18nDictionaryValue.setStatus( arg0.getStatus() );
        }
        if ( arg0.getAppId() != null ) {
            i18nDictionaryValue.setAppId( arg0.getAppId() );
        }
        if ( arg0.getDictionaryKeyId() != null ) {
            i18nDictionaryValue.setDictionaryKeyId( arg0.getDictionaryKeyId() );
        }
        if ( arg0.getDictionaryValue() != null ) {
            i18nDictionaryValue.setDictionaryValue( arg0.getDictionaryValue() );
        }
        if ( arg0.getLocale() != null ) {
            i18nDictionaryValue.setLocale( arg0.getLocale() );
        }

        return i18nDictionaryValue;
    }

    @Override
    public I18nDictionaryValueDTO toDTO(I18nDictionaryValue arg0) {
        if ( arg0 == null ) {
            return null;
        }

        I18nDictionaryValueDTO i18nDictionaryValueDTO = new I18nDictionaryValueDTO();

        if ( arg0.getId() != null ) {
            i18nDictionaryValueDTO.setId( arg0.getId() );
        }
        if ( arg0.getAppId() != null ) {
            i18nDictionaryValueDTO.setAppId( arg0.getAppId() );
        }
        if ( arg0.getDictionaryKeyId() != null ) {
            i18nDictionaryValueDTO.setDictionaryKeyId( arg0.getDictionaryKeyId() );
        }
        if ( arg0.getDictionaryValue() != null ) {
            i18nDictionaryValueDTO.setDictionaryValue( arg0.getDictionaryValue() );
        }
        if ( arg0.getLocale() != null ) {
            i18nDictionaryValueDTO.setLocale( arg0.getLocale() );
        }
        if ( arg0.getCreateTime() != null ) {
            i18nDictionaryValueDTO.setCreateTime( arg0.getCreateTime() );
        }
        if ( arg0.getUpdateTime() != null ) {
            i18nDictionaryValueDTO.setUpdateTime( arg0.getUpdateTime() );
        }
        if ( arg0.getCreateUser() != null ) {
            i18nDictionaryValueDTO.setCreateUser( arg0.getCreateUser() );
        }
        if ( arg0.getUpdateUser() != null ) {
            i18nDictionaryValueDTO.setUpdateUser( arg0.getUpdateUser() );
        }
        if ( arg0.getCreateUserId() != null ) {
            i18nDictionaryValueDTO.setCreateUserId( arg0.getCreateUserId() );
        }
        if ( arg0.getUpdateUserId() != null ) {
            i18nDictionaryValueDTO.setUpdateUserId( arg0.getUpdateUserId() );
        }
        if ( arg0.getStatus() != null ) {
            i18nDictionaryValueDTO.setStatus( arg0.getStatus() );
        }

        return i18nDictionaryValueDTO;
    }
}
