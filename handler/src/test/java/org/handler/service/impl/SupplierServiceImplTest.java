package org.handler.service.impl;

import org.handler.dto.request.SupplierRequestDto;
import org.handler.dto.response.SupplierResponseDto;
import org.handler.exception.SupplierAlreadyExistsException;
import org.handler.exception.SupplierNotFoundException;
import org.handler.mapper.SupplierMapper;
import org.handler.model.Supplier;
import org.handler.model.enums.SupplierSource;
import org.handler.repository.SupplierRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SupplierServiceImplTest {

    @Mock
    private SupplierRepository supplierRepository;
    @Mock
    private SupplierMapper supplierMapper;

    @InjectMocks
    private SupplierServiceImpl supplierService;

    @Test
    void createSupplier_ShouldSanitizeNameSaveAndReturnResponse_WhenSupplierIsNew() {
        SupplierRequestDto request = new SupplierRequestDto();
        request.setName("  ACME@@  Works###  ");
        request.setSource("MANUAL");

        Supplier mappedEntity = Supplier.builder().source(SupplierSource.MANUAL).build();
        Supplier savedEntity = Supplier.builder().id(1L).name("ACME Works").source(SupplierSource.MANUAL).build();
        SupplierResponseDto expectedResponse = new SupplierResponseDto();
        expectedResponse.setId(1L);
        expectedResponse.setName("ACME Works");

        when(supplierRepository.findByName("ACME Works")).thenReturn(Optional.empty());
        when(supplierMapper.toEntity(request)).thenReturn(mappedEntity);
        when(supplierRepository.save(mappedEntity)).thenReturn(savedEntity);
        when(supplierMapper.toResponseDto(savedEntity)).thenReturn(expectedResponse);

        SupplierResponseDto actual = supplierService.createSupplier(request);

        assertSame(expectedResponse, actual);
        assertEquals("ACME Works", mappedEntity.getName());
        verify(supplierRepository).findByName("ACME Works");
        verify(supplierMapper).toEntity(request);
        verify(supplierRepository).save(mappedEntity);
        verify(supplierMapper).toResponseDto(savedEntity);
    }

    @Test
    void createSupplier_ShouldThrowSupplierAlreadyExistsException_WhenSupplierNameAlreadyExists() {
        SupplierRequestDto request = new SupplierRequestDto();
        request.setName("Existing Supplier");

        Supplier existing = Supplier.builder().id(9L).name("Existing Supplier").source(SupplierSource.AI).build();
        SupplierResponseDto existingDto = new SupplierResponseDto();
        existingDto.setId(9L);
        existingDto.setName("Existing Supplier");

        when(supplierRepository.findByName("Existing Supplier")).thenReturn(Optional.of(existing));
        when(supplierMapper.toResponseDto(existing)).thenReturn(existingDto);

        SupplierAlreadyExistsException exception = assertThrows(
                SupplierAlreadyExistsException.class,
                () -> supplierService.createSupplier(request)
        );

        assertEquals("Supplier already exists with name: Existing Supplier", exception.getMessage());
        assertSame(existingDto, exception.getSupplier());
        verify(supplierMapper, never()).toEntity(any());
        verify(supplierRepository, never()).save(any());
    }


    @Test
    void getAllSuppliers_ShouldReturnMappedResponseList_WhenSuppliersExist() {
        Supplier supplierOne = Supplier.builder().id(1L).name("A").build();
        Supplier supplierTwo = Supplier.builder().id(2L).name("B").build();

        SupplierResponseDto dtoOne = new SupplierResponseDto();
        dtoOne.setId(1L);
        SupplierResponseDto dtoTwo = new SupplierResponseDto();
        dtoTwo.setId(2L);

        List<Supplier> suppliers = List.of(supplierOne, supplierTwo);
        List<SupplierResponseDto> expected = List.of(dtoOne, dtoTwo);

        when(supplierRepository.findAll()).thenReturn(suppliers);
        when(supplierMapper.toResponseDtoList(suppliers)).thenReturn(expected);

        List<SupplierResponseDto> actual = supplierService.getAllSuppliers();

        assertSame(expected, actual);
        verify(supplierRepository).findAll();
        verify(supplierMapper).toResponseDtoList(suppliers);
    }

    @Test
    void findSupplierById_ShouldReturnSupplier_WhenSupplierExists() {
        Supplier supplier = Supplier.builder().id(77L).name("Supplier77").build();
        when(supplierRepository.findById(77L)).thenReturn(Optional.of(supplier));

        Supplier result = supplierService.findSupplierById(77L);

        assertSame(supplier, result);
        verify(supplierRepository).findById(77L);
    }

    @Test
    void findSupplierById_ShouldThrowSupplierNotFoundException_WhenSupplierMissing() {
        when(supplierRepository.findById(88L)).thenReturn(Optional.empty());

        SupplierNotFoundException exception = assertThrows(
                SupplierNotFoundException.class,
                () -> supplierService.findSupplierById(88L)
        );

        assertEquals("Supplier not found with ID: 88", exception.getMessage());
    }


    @Test
    void createSupplier_ShouldNormalizeMultipleSpacesAndKeepAllowedCharacters() {
        SupplierRequestDto request = new SupplierRequestDto();
        request.setName("  UAB   Keliai  &  Tiltai (LT)  ");

        Supplier mappedEntity = Supplier.builder().build();
        Supplier savedEntity = Supplier.builder().id(3L).name("UAB Keliai & Tiltai (LT)").build();
        SupplierResponseDto responseDto = new SupplierResponseDto();
        responseDto.setId(3L);

        when(supplierRepository.findByName("UAB Keliai & Tiltai (LT)")).thenReturn(Optional.empty());
        when(supplierMapper.toEntity(request)).thenReturn(mappedEntity);
        when(supplierRepository.save(mappedEntity)).thenReturn(savedEntity);
        when(supplierMapper.toResponseDto(savedEntity)).thenReturn(responseDto);

        supplierService.createSupplier(request);

        ArgumentCaptor<Supplier> supplierCaptor = ArgumentCaptor.forClass(Supplier.class);
        verify(supplierRepository).save(supplierCaptor.capture());
        assertEquals("UAB Keliai & Tiltai (LT)", supplierCaptor.getValue().getName());
    }
}
