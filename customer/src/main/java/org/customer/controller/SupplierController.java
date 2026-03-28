package org.customer.controller;

import lombok.RequiredArgsConstructor;
import org.handler.dto.request.SupplierRequestDto;
import org.handler.dto.response.SupplierResponseDto;
import org.handler.service.SupplierService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/suppliers")
@RequiredArgsConstructor
public class SupplierController {
    private final SupplierService supplierService;

    @PostMapping
    public SupplierResponseDto createSupplier(@RequestBody SupplierRequestDto requestDto) {
        return supplierService.createSupplier(requestDto);
    }

    @GetMapping
    public List<SupplierResponseDto> getAllSuppliers() {
        return supplierService.getAllSuppliers();
    }
}
