package org.handler.service;

import org.handler.dto.request.CaseRequestDto;
import org.handler.dto.request.PaginationRequest;
import org.handler.dto.response.CaseResponseDto;
import org.handler.dto.response.PaginationResponse;
import org.handler.model.Case;
import org.handler.model.enums.CaseStatus;
import org.handler.model.enums.CaseType;
import org.springframework.ai.document.Document;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface CaseService {
    CaseResponseDto createCase(CaseRequestDto caseRequestDto,  List<MultipartFile> photos);

    CaseResponseDto getCaseById(Long id);

    List<CaseResponseDto> getAllCases();

    void deleteCase(Long id);

    Long getCaseCountForCurrentUser();

    PaginationResponse<CaseResponseDto> getUserCases(PaginationRequest paginationRequest, CaseStatus status, CaseType type);

    Case findCaseById(Long caseId);

    String suggestCompaniesForCase(Long caseId);
}
