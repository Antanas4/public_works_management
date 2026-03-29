package org.handler.service.impl;

import lombok.RequiredArgsConstructor;
import org.handler.dto.request.SupplierRequestDto;
import org.handler.dto.response.SupplierResponseDto;
import org.handler.exception.SupplierAlreadyExistsException;
import org.handler.exception.SupplierNotFoundException;
import org.handler.mapper.SupplierMapper;
import org.handler.model.Supplier;
import org.handler.model.enums.CaseSubtype;
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
    public SupplierResponseDto createSupplier(SupplierRequestDto supplierRequestDto) {
        String cleanedName = sanitizeName(supplierRequestDto.getName());

        supplierRepository.findByName(cleanedName).ifPresent(existingSupplier -> {
                    throw new SupplierAlreadyExistsException("Supplier already exists with name: " + cleanedName,
                            supplierMapper.toResponseDto((Supplier) existingSupplier));});

        Supplier supplier = supplierMapper.toEntity(supplierRequestDto);

        supplier.setName(cleanedName);

        return supplierMapper.toResponseDto(
                supplierRepository.save(supplier)
        );
    }

    @Override
    public List<SupplierResponseDto> getAllSuppliers() {
        List<Supplier> suppliers = supplierRepository.findAll();
        return supplierMapper.toResponseDtoList(suppliers);
    }

    public Supplier findSupplierById(Long supplierId) {
        return supplierRepository.findById(supplierId)
                .orElseThrow(() -> new SupplierNotFoundException("Supplier not found with ID: " + supplierId));
    }

    private String sanitizeName(String name) {
        if (name == null) {
            return null;
        }

        return name
                .replaceAll("[^\\p{L}0-9\\s&()-]", "")
                .trim()
                .replaceAll("\\s+", " ");
    }
}