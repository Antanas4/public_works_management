package org.handler.service;

import org.handler.dto.request.CaseRequestDto;
import org.handler.dto.response.SupplierResponseDto;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface AiService {
    CompletableFuture<String> generateQuestionsForRequestCase(Long caseId, CaseRequestDto caseRequestDto);

    List<SupplierResponseDto> generateSupplierSuggestionsRag(String query, String cpvPrefix);
}
