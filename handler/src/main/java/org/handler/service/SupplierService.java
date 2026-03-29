package org.handler.service;

import org.handler.dto.request.SupplierRequestDto;
import org.handler.dto.response.SupplierResponseDto;

import java.util.List;

public interface SupplierService {
    SupplierResponseDto createSupplier(SupplierRequestDto requestDto);

    List<SupplierResponseDto> getAllSuppliers();

    void assignSupplierToCase(Long caseId, SupplierRequestDto supplierRequestDto);
}
