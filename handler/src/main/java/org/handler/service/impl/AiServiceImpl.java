package org.handler.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.handler.config.AiConfig;
import org.handler.dto.request.CaseRequestDto;
import org.handler.dto.response.SupplierResponseDto;
import org.handler.exception.PromptNotFoundException;
import org.handler.service.AiService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class AiServiceImpl implements AiService {
    private static final String ENVIRONMENT_CASE_PROMPT_KEY = "environment-case";
    private static final String SUPPLIER_SUGGESTIONS_PROMPT_KEY = "supplier-suggestions";
    private static final String SUPPLIER_NAMES = "supplier_names";
    private static final String BUYER = "buyer";
    private static final String CPV_CODES = "cpv_codes";

    private final ChatClient chatClient;
    private final AiConfig aiConfig;
    private final VectorStore vectorStore;

    public AiServiceImpl(ChatClient.Builder builder, AiConfig aiConfig, VectorStore vectorStore) {
        this.chatClient = builder.build();
        this.aiConfig = aiConfig;
        this.vectorStore = vectorStore;
    }

    @Async
    @Override
    public CompletableFuture<String> generateQuestionsForRequestCase(Long caseId, CaseRequestDto caseRequestDto) {
        log.info("Generating question for case with id {}", caseId);

        Map<String, String> parameters = caseRequestDto.getParameters();
        String promptTemplate = aiConfig.getPrompt(ENVIRONMENT_CASE_PROMPT_KEY)
                .orElseThrow(() -> new PromptNotFoundException("Prompt not found with key: " + ENVIRONMENT_CASE_PROMPT_KEY));

        String prompt = formatServiceRequestPrompt(parameters, promptTemplate);
        String response = chatClient.prompt().user(prompt).call().content();

        log.info("Ai response generated for case with id {}", caseId);
        return CompletableFuture.completedFuture(response);
    }

    @Override
    public List<SupplierResponseDto> generateSupplierSuggestionsRag(String query, String cpvPrefix) {
        List<Document> docs = retrieveSupplierDocuments(query, cpvPrefix);
        Map<String, List<Document>> supplierDocs = extractSupplierEvidence(docs);

        if (supplierDocs.isEmpty()) {
            log.warn("No suppliers found for query {}", query);
            return List.of();
        }
        String supplierContext = buildSupplierContext(supplierDocs);
        String prompt = buildSupplierSuggestionPrompt(query, supplierContext);

        SupplierResponseDto[] result =
                chatClient.prompt()
                        .user(prompt)
                        .call()
                        .entity(SupplierResponseDto[].class);

        return Arrays.asList(result);
    }

    private List<Document> retrieveSupplierDocuments(String query, String cpvPrefix) {
        List<Document> docs =
                vectorStore.similaritySearch(
                        SearchRequest.builder()
                                .query(query)
                                .topK(15)
                                .filterExpression(
                                        new FilterExpressionBuilder()
                                                .in("cpv_prefixes", cpvPrefix)
                                                .build()
                                )
                                .build()
                );

        log.info("Retrieved {} supplier docs", docs.size());

        return docs;
    }

    private Map<String, List<Document>> extractSupplierEvidence(List<Document> docs) {
        Map<String, List<Document>> supplierDocs = new LinkedHashMap<>();
        for (Document doc : docs) {
            Object suppliersMeta = doc.getMetadata().get(SUPPLIER_NAMES);

            if (!(suppliersMeta instanceof List<?> supplierList)) continue;
            for (Object supplierObj : supplierList) {
                String supplierName = supplierObj.toString();
                supplierDocs
                        .computeIfAbsent(supplierName, k -> new ArrayList<>())
                        .add(doc);
            }
        }
        return supplierDocs;
    }

    private String buildSupplierContext(Map<String, List<Document>> supplierDocs) {
        StringBuilder context = new StringBuilder();

        supplierDocs.forEach((supplier, docs) -> {
            context.append("- ")
                    .append(supplier)
                    .append("\n");
            docs.stream()
                    .limit(2)
                    .forEach(doc -> {
                        context.append("  Ankstesnis projektas:\n");
                        context.append("  ")
                                .append(doc.getText())
                                .append("\n");
                        context.append("  Pirkėjas: ")
                                .append(doc.getMetadata().get(BUYER))
                                .append("\n");
                        context.append("  CPV: ")
                                .append(doc.getMetadata().get(CPV_CODES))
                                .append("\n\n");
                    });
        });

        return context.toString();
    }

    private String buildSupplierSuggestionPrompt(String query,String supplierContext) {
        String template = aiConfig.getPrompt(SUPPLIER_SUGGESTIONS_PROMPT_KEY).orElseThrow(() ->
                        new PromptNotFoundException("Prompt not found: " + SUPPLIER_SUGGESTIONS_PROMPT_KEY));

        return String.format(template, supplierContext, query);
    }

    private String formatServiceRequestPrompt(Map<String, String> parameters, String promptTemplate) {
        String formattedServiceDetails = parameters.entrySet()
                .stream()
                .map(e -> "- " + e.getKey() + ": " + e.getValue())
                .reduce("Service request details:\n", (key, value) -> key + value + "\n");

        return String.format(promptTemplate, formattedServiceDetails);
    }
}
