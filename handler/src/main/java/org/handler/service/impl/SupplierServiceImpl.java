package org.handler.service.impl;

import lombok.RequiredArgsConstructor;
import org.handler.dto.request.SupplierRequestDto;
import org.handler.dto.response.SupplierResponseDto;
import org.handler.exception.CaseNotFoundException;
import org.handler.exception.SupplierNotFoundException;
import org.handler.mapper.SupplierMapper;
import org.handler.model.Case;
import org.handler.model.Supplier;
import org.handler.model.enums.SupplierSource;
import org.handler.repository.SupplierRepository;
import org.handler.service.CaseService;
import org.handler.service.SupplierService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SupplierServiceImpl implements SupplierService {
    private final SupplierRepository supplierRepository;
    private final SupplierMapper supplierMapper;
    private final CaseService caseService;

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

    @Override
    public void assignSupplierToCase(Long caseId, SupplierRequestDto supplierRequestDto) {
        if (supplierRequestDto.getSource().equals(SupplierSource.AI.toString())) {
            Supplier supplier = supplierMapper.toEntity(supplierRequestDto);
            Supplier savedSupplier = supplierRepository.save(supplier);
            caseService.setSupplier(savedSupplier, caseId);
        } else {
            Supplier supplier = findSupplierById(supplierRequestDto.getId());
            caseService.setSupplier(supplier, caseId);
        }
    }

    public Supplier findSupplierById(Long caseId) {
        return supplierRepository.findById(caseId)
                .orElseThrow(() -> new SupplierNotFoundException("Case not found with ID: " + caseId));
    }
}