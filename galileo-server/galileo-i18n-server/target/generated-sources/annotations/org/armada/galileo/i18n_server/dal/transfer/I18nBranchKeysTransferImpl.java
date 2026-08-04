package org.armada.galileo.i18n_server.dal.transfer;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.processing.Generated;
import org.armada.galileo.i18n_server.dal.dto.I18nBranchKeysDTO;
import org.armada.galileo.i18n_server.dal.entity.I18nBranchKeys;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-08-03T20:11:45+0800",
    comments = "version: 1.5.0.Beta2, compiler: javac, environment: Java 11.0.27 (Azul Systems, Inc.)"
)
@Component
public class I18nBranchKeysTransferImpl implements I18nBranchKeysTransfer {

    @Override
    public I18nBranchKeys toDO(I18nBranchKeysDTO arg0) {
        if ( arg0 == null ) {
            return null;
        }

        I18nBranchKeys i18nBranchKeys = new I18nBranchKeys();

        if ( arg0.getAppId() != null ) {
            i18nBranchKeys.setAppId( arg0.getAppId() );
        }
        if ( arg0.getBranchType() != null ) {
            i18nBranchKeys.setBranchType( arg0.getBranchType() );
        }
        if ( arg0.getBranchPath() != null ) {
            i18nBranchKeys.setBranchPath( arg0.getBranchPath() );
        }
        Set<String> set = arg0.getDictionaryKeys();
        if ( set != null ) {
            i18nBranchKeys.setDictionaryKeys( new LinkedHashSet<String>( set ) );
        }
        if ( arg0.getStatus() != null ) {
            i18nBranchKeys.setStatus( arg0.getStatus() );
        }

        return i18nBranchKeys;
    }

    @Override
    public I18nBranchKeysDTO toDTO(I18nBranchKeys arg0) {
        if ( arg0 == null ) {
            return null;
        }

        I18nBranchKeysDTO i18nBranchKeysDTO = new I18nBranchKeysDTO();

        if ( arg0.getAppId() != null ) {
            i18nBranchKeysDTO.setAppId( arg0.getAppId() );
        }
        if ( arg0.getBranchType() != null ) {
            i18nBranchKeysDTO.setBranchType( arg0.getBranchType() );
        }
        if ( arg0.getBranchPath() != null ) {
            i18nBranchKeysDTO.setBranchPath( arg0.getBranchPath() );
        }
        Set<String> set = arg0.getDictionaryKeys();
        if ( set != null ) {
            i18nBranchKeysDTO.setDictionaryKeys( new LinkedHashSet<String>( set ) );
        }
        if ( arg0.getStatus() != null ) {
            i18nBranchKeysDTO.setStatus( arg0.getStatus() );
        }

        return i18nBranchKeysDTO;
    }

    @Override
    public List<I18nBranchKeysDTO> toListDTO(List<I18nBranchKeys> arg0) {
        if ( arg0 == null ) {
            return null;
        }

        List<I18nBranchKeysDTO> list = new ArrayList<I18nBranchKeysDTO>( arg0.size() );
        for ( I18nBranchKeys i18nBranchKeys : arg0 ) {
            list.add( toDTO( i18nBranchKeys ) );
        }

        return list;
    }

    @Override
    public List<I18nBranchKeys> toListDO(List<I18nBranchKeysDTO> arg0) {
        if ( arg0 == null ) {
            return null;
        }

        List<I18nBranchKeys> list = new ArrayList<I18nBranchKeys>( arg0.size() );
        for ( I18nBranchKeysDTO i18nBranchKeysDTO : arg0 ) {
            list.add( toDO( i18nBranchKeysDTO ) );
        }

        return list;
    }
}
