package org.armada.galileo.mybatis.bo;

public interface MapstructConvertor<DO, DTO> {

	DO toDO(DTO dtoObj);

	DTO toDTO(DO doObj);

}
