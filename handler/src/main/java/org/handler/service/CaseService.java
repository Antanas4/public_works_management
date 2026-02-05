package org.handler.service;

import org.handler.dto.request.CaseRequestDto;
import org.handler.dto.request.PaginationRequest;
import org.handler.dto.response.CaseResponseDto;
import org.handler.dto.response.PaginationResponse;
import org.handler.model.Case;
import org.handler.model.enums.CaseStatus;
import org.handler.model.enums.CaseType;

import java.util.List;

public interface CaseService {
    CaseResponseDto createCase(CaseRequestDto caseRequestDto);

    CaseResponseDto getCaseById(Long id);

    List<CaseResponseDto> getAllCases();

    void deleteCase(Long id);

    Long getCaseCountByUserId(Long userId);

    PaginationResponse<CaseResponseDto> getCasesByUserId(Long userId, PaginationRequest paginationRequest, CaseStatus status, CaseType type);

    Case findCaseById(Long caseId);
}
