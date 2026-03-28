package org.handler.mapper;

import org.handler.dto.request.SupplierRequestDto;
import org.handler.dto.response.SupplierResponseDto;
import org.handler.model.Supplier;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SupplierMapper {
    Supplier toEntity(SupplierRequestDto dto);
    SupplierResponseDto toResponseDto(Supplier supplier);
    List<SupplierResponseDto> toResponseDtoList(List<Supplier> suppliers);
}