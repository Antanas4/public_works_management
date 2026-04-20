package org.handler.service;

import jakarta.transaction.Transactional;
import org.handler.dto.request.CaseRequestDto;
import org.handler.dto.request.PaginationRequest;
import org.handler.dto.request.SupplierRequestDto;
import org.handler.dto.response.CaseResponseDto;
import org.handler.dto.response.PaginationResponse;
import org.handler.dto.response.SupplierResponseDto;
import org.handler.model.Case;
import org.handler.model.Supplier;
import org.handler.model.enums.CaseStatus;
import org.handler.model.enums.CaseType;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface CaseService {
    CaseResponseDto createCase(CaseRequestDto caseRequestDto,  List<MultipartFile> photos);

    CaseResponseDto getCaseById(Long id);

    PaginationResponse<CaseResponseDto> getAllCases(PaginationRequest paginationRequest, CaseStatus status, CaseType type);

    void deleteCase(Long id);

    Long getCaseCountForCurrentUser();

    PaginationResponse<CaseResponseDto> getUserCases(PaginationRequest paginationRequest, CaseStatus status, CaseType type);

    Case findCaseById(Long caseId);

    Case findCaseByIdWithPhotos(Long caseId);

    List<SupplierResponseDto> suggestSuppliersForCase(Long caseId);

    @Transactional
    void assignSupplier(Long caseId, Long supplierId);

    CaseResponseDto updateCase(Long caseId, CaseRequestDto caseRequestDto);
}
