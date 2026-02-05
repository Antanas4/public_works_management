package org.handler.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.handler.config.AiConfig;
import org.handler.dto.request.CaseRequestDto;
import org.handler.exception.PromptNotFoundException;
import org.handler.service.AiService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class AiServiceImpl implements AiService {
    private final ChatClient chatClient;
    private final AiConfig aiConfig;
    private final String REQUEST_CASE_PROMPT_KEY = "request-case";

    public AiServiceImpl(ChatClient.Builder builder, AiConfig aiConfig) {
        this.chatClient = builder.build();
        this.aiConfig = aiConfig;
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

    private String formatServiceRequestPrompt (Map<String, String> parameters, String promptTemplate) {
        String formattedServiceDetails = parameters.entrySet()
                .stream()
                .map(e -> "- " + e.getKey() + ": " + e.getValue())
                .reduce("Service request details:\n", (key, value) -> key + value + "\n");

        return String.format(promptTemplate, formattedServiceDetails);
    }
}
