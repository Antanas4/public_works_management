package org.handler.service;

import org.handler.dto.request.SupplierRequestDto;
import org.handler.dto.response.SupplierResponseDto;
import org.handler.model.Supplier;

import java.util.List;

public interface SupplierService {
    SupplierResponseDto createSupplier(SupplierRequestDto requestDto);

    List<SupplierResponseDto> getAllSuppliers();

    Supplier findSupplierById(Long supplierId);
}
