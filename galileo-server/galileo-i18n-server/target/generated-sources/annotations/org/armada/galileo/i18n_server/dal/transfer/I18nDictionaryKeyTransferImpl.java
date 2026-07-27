package org.armada.galileo.i18n_server.dal.transfer;

import javax.annotation.processing.Generated;
import org.armada.galileo.i18n_server.dal.dto.I18nDictionaryKeyDTO;
import org.armada.galileo.i18n_server.dal.entity.I18nDictionaryKey;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-07-27T15:50:16+0800",
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
        if ( arg0.getAppId() != null ) {
            i18nDictionaryKey.setAppId( arg0.getAppId() );
        }
        if ( arg0.getAppCode() != null ) {
            i18nDictionaryKey.setAppCode( arg0.getAppCode() );
        }
        if ( arg0.getDictionaryKey() != null ) {
            i18nDictionaryKey.setDictionaryKey( arg0.getDictionaryKey() );
        }
        if ( arg0.getStatus() != null ) {
            i18nDictionaryKey.setStatus( arg0.getStatus() );
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
        if ( arg0.getStatus() != null ) {
            i18nDictionaryKeyDTO.setStatus( arg0.getStatus() );
        }

        return i18nDictionaryKeyDTO;
    }
}
