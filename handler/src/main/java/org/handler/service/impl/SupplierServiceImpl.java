package org.handler.service.impl;

import lombok.RequiredArgsConstructor;
import org.handler.dto.request.SupplierRequestDto;
import org.handler.dto.response.SupplierResponseDto;
import org.handler.mapper.SupplierMapper;
import org.handler.model.Supplier;
import org.handler.repository.SupplierRepository;
import org.handler.service.SupplierService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SupplierServiceImpl implements SupplierService {

    private final SupplierRepository supplierRepository;
    private final SupplierMapper supplierMapper;

    @Override
    public SupplierResponseDto createSupplier(SupplierRequestDto requestDto) {
        Supplier supplier = supplierMapper.toEntity(requestDto);
        Supplier savedSupplier = supplierRepository.save(supplier);

        return supplierMapper.toResponseDto(savedSupplier);
    }

    @Override
    public List<SupplierResponseDto> getAllSuppliers() {
        List<Supplier> suppliers = supplierRepository.findAll();
        return supplierMapper.toResponseDtoList(suppliers);
    }
}