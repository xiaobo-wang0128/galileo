package org.armada.galileo.i18n_server.dal.transfer;

import java.util.LinkedHashSet;
import java.util.Set;
import javax.annotation.processing.Generated;
import org.armada.galileo.i18n_server.dal.dto.I18nBranchKeysDTO;
import org.armada.galileo.i18n_server.dal.entity.I18nBranchKeys;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-06-03T17:08:40+0800",
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

        if ( arg0.getCreateTime() != null ) {
            i18nBranchKeys.setCreateTime( arg0.getCreateTime() );
        }
        if ( arg0.getUpdateTime() != null ) {
            i18nBranchKeys.setUpdateTime( arg0.getUpdateTime() );
        }
        if ( arg0.getCreateUser() != null ) {
            i18nBranchKeys.setCreateUser( arg0.getCreateUser() );
        }
        if ( arg0.getUpdateUser() != null ) {
            i18nBranchKeys.setUpdateUser( arg0.getUpdateUser() );
        }
        if ( arg0.getCreateUserId() != null ) {
            i18nBranchKeys.setCreateUserId( arg0.getCreateUserId() );
        }
        if ( arg0.getUpdateUserId() != null ) {
            i18nBranchKeys.setUpdateUserId( arg0.getUpdateUserId() );
        }
        if ( arg0.getStatus() != null ) {
            i18nBranchKeys.setStatus( arg0.getStatus() );
        }
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
        if ( arg0.getCreateTime() != null ) {
            i18nBranchKeysDTO.setCreateTime( arg0.getCreateTime() );
        }
        if ( arg0.getUpdateTime() != null ) {
            i18nBranchKeysDTO.setUpdateTime( arg0.getUpdateTime() );
        }
        if ( arg0.getCreateUser() != null ) {
            i18nBranchKeysDTO.setCreateUser( arg0.getCreateUser() );
        }
        if ( arg0.getUpdateUser() != null ) {
            i18nBranchKeysDTO.setUpdateUser( arg0.getUpdateUser() );
        }
        if ( arg0.getCreateUserId() != null ) {
            i18nBranchKeysDTO.setCreateUserId( arg0.getCreateUserId() );
        }
        if ( arg0.getUpdateUserId() != null ) {
            i18nBranchKeysDTO.setUpdateUserId( arg0.getUpdateUserId() );
        }
        if ( arg0.getStatus() != null ) {
            i18nBranchKeysDTO.setStatus( arg0.getStatus() );
        }

        return i18nBranchKeysDTO;
    }
}
