package org.handler.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.handler.config.AiConfig;
import org.handler.dto.request.CaseRequestDto;
import org.handler.exception.PromptNotFoundException;
import org.handler.service.AiService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class AiServiceImpl implements AiService {
    private final ChatClient chatClient;
    private final AiConfig aiConfig;
    private final VectorStore vectorStore;
    private final String REQUEST_CASE_PROMPT_KEY = "request-case";

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
        String promptTemplate = aiConfig.getPrompt(REQUEST_CASE_PROMPT_KEY)
                .orElseThrow(() -> new PromptNotFoundException("Prompt not found with key: " + REQUEST_CASE_PROMPT_KEY));

        String prompt = formatServiceRequestPrompt(parameters, promptTemplate);
        String response = chatClient.prompt().user(prompt).call().content();

        log.info("Ai response generated for case with id {}", caseId);
        return CompletableFuture.completedFuture(response);
    }

    @Override
    public String generateSupplierSuggestionsRag(String query, String cpvPrefix) {

        log.info("Running RAG supplier suggestion pipeline");

        RetrievalAugmentationAdvisor advisor =
                RetrievalAugmentationAdvisor.builder()
                        .documentRetriever(
                                VectorStoreDocumentRetriever.builder()
                                        .vectorStore(vectorStore)
                                        .topK(10)
                                        .filterExpression(new FilterExpressionBuilder()
                                                .in("cpv_prefixes", cpvPrefix)
                                                .build())
//                                        .similarityThreshold(0.50)
                                        .build()
                        )
                        .build();


        String prompt = """
                You are a Lithuanian public procurement assistant.
                
                Use ONLY suppliers from the retrieved contract dataset.
                
                Do NOT invent suppliers.
                
                Return JSON array:
                
                [
                  {
                    "supplierName": "...",
                    "reason": "...",
                    "confidence": 0.0
                  }
                ]
                
                Confidence must reflect similarity between supplier contract history and request.
                """;

        String response = chatClient.prompt()
                .advisors(advisor)
                .user(prompt + "\n\nUser request:\n" + query)
                .call()
                .content();

        return response;
    }

    private String formatServiceRequestPrompt(Map<String, String> parameters, String promptTemplate) {
        String formattedServiceDetails = parameters.entrySet()
                .stream()
                .map(e -> "- " + e.getKey() + ": " + e.getValue())
                .reduce("Service request details:\n", (key, value) -> key + value + "\n");

        return String.format(promptTemplate, formattedServiceDetails);
    }
}
