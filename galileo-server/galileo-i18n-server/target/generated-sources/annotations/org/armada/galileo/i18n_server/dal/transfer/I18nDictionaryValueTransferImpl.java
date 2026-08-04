package org.armada.galileo.i18n_server.dal.transfer;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.armada.galileo.i18n_server.dal.dto.I18nDictionaryValueDTO;
import org.armada.galileo.i18n_server.dal.entity.I18nDictionaryValue;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-08-03T20:11:45+0800",
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
        if ( arg0.getStatus() != null ) {
            i18nDictionaryValue.setStatus( arg0.getStatus() );
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
        if ( arg0.getStatus() != null ) {
            i18nDictionaryValueDTO.setStatus( arg0.getStatus() );
        }

        return i18nDictionaryValueDTO;
    }

    @Override
    public List<I18nDictionaryValueDTO> toListDTO(List<I18nDictionaryValue> arg0) {
        if ( arg0 == null ) {
            return null;
        }

        List<I18nDictionaryValueDTO> list = new ArrayList<I18nDictionaryValueDTO>( arg0.size() );
        for ( I18nDictionaryValue i18nDictionaryValue : arg0 ) {
            list.add( toDTO( i18nDictionaryValue ) );
        }

        return list;
    }

    @Override
    public List<I18nDictionaryValue> toListDO(List<I18nDictionaryValueDTO> arg0) {
        if ( arg0 == null ) {
            return null;
        }

        List<I18nDictionaryValue> list = new ArrayList<I18nDictionaryValue>( arg0.size() );
        for ( I18nDictionaryValueDTO i18nDictionaryValueDTO : arg0 ) {
            list.add( toDO( i18nDictionaryValueDTO ) );
        }

        return list;
    }
}
