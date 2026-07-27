package org.armada.galileo.mybatis.bo;

import java.util.List;

public interface MapstructConvertor<DO, DTO> {

    DO toDO(DTO dtoObj);

    DTO toDTO(DO doObj);

    List<DTO> toListDTO(List<DO> list);

    List<DO> toListDO(List<DTO> list);
}
