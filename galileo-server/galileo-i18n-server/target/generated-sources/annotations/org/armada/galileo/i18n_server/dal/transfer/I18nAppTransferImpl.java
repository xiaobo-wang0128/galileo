package org.armada.galileo.i18n_server.dal.transfer;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.armada.galileo.i18n_server.dal.dto.I18nAppDTO;
import org.armada.galileo.i18n_server.dal.entity.I18nApp;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-07-27T20:46:10+0800",
    comments = "version: 1.5.0.Beta2, compiler: javac, environment: Java 11.0.27 (Azul Systems, Inc.)"
)
@Component
public class I18nAppTransferImpl implements I18nAppTransfer {

    @Override
    public I18nApp toDO(I18nAppDTO arg0) {
        if ( arg0 == null ) {
            return null;
        }

        I18nApp i18nApp = new I18nApp();

        if ( arg0.getId() != null ) {
            i18nApp.setId( arg0.getId() );
        }
        if ( arg0.getAppCode() != null ) {
            i18nApp.setAppCode( arg0.getAppCode() );
        }
        if ( arg0.getAppName() != null ) {
            i18nApp.setAppName( arg0.getAppName() );
        }
        if ( arg0.getAppDescribe() != null ) {
            i18nApp.setAppDescribe( arg0.getAppDescribe() );
        }
        List<String> list = arg0.getLocales();
        if ( list != null ) {
            i18nApp.setLocales( new ArrayList<String>( list ) );
        }
        if ( arg0.getExportType() != null ) {
            i18nApp.setExportType( arg0.getExportType() );
        }
        if ( arg0.getStatus() != null ) {
            i18nApp.setStatus( arg0.getStatus() );
        }

        return i18nApp;
    }

    @Override
    public I18nAppDTO toDTO(I18nApp arg0) {
        if ( arg0 == null ) {
            return null;
        }

        I18nAppDTO i18nAppDTO = new I18nAppDTO();

        if ( arg0.getId() != null ) {
            i18nAppDTO.setId( arg0.getId() );
        }
        if ( arg0.getAppCode() != null ) {
            i18nAppDTO.setAppCode( arg0.getAppCode() );
        }
        if ( arg0.getAppName() != null ) {
            i18nAppDTO.setAppName( arg0.getAppName() );
        }
        if ( arg0.getAppDescribe() != null ) {
            i18nAppDTO.setAppDescribe( arg0.getAppDescribe() );
        }
        List<String> list = arg0.getLocales();
        if ( list != null ) {
            i18nAppDTO.setLocales( new ArrayList<String>( list ) );
        }
        if ( arg0.getExportType() != null ) {
            i18nAppDTO.setExportType( arg0.getExportType() );
        }
        if ( arg0.getStatus() != null ) {
            i18nAppDTO.setStatus( arg0.getStatus() );
        }

        return i18nAppDTO;
    }

    @Override
    public List<I18nAppDTO> toListDTO(List<I18nApp> arg0) {
        if ( arg0 == null ) {
            return null;
        }

        List<I18nAppDTO> list = new ArrayList<I18nAppDTO>( arg0.size() );
        for ( I18nApp i18nApp : arg0 ) {
            list.add( toDTO( i18nApp ) );
        }

        return list;
    }

    @Override
    public List<I18nApp> toListDO(List<I18nAppDTO> arg0) {
        if ( arg0 == null ) {
            return null;
        }

        List<I18nApp> list = new ArrayList<I18nApp>( arg0.size() );
        for ( I18nAppDTO i18nAppDTO : arg0 ) {
            list.add( toDO( i18nAppDTO ) );
        }

        return list;
    }
}
