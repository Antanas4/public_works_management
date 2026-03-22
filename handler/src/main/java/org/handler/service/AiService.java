package org.handler.service;

import org.handler.dto.request.CaseRequestDto;

import java.util.concurrent.CompletableFuture;

public interface AiService {
    CompletableFuture<String> generateQuestionsForRequestCase(Long caseId, CaseRequestDto caseRequestDto);

    String generateSupplierSuggestionsRag(String query, String cpvPrefix);
}
