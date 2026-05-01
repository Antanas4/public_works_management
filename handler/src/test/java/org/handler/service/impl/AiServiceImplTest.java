package org.handler.service.impl;

import org.handler.config.AiConfig;
import org.handler.dto.request.CaseRequestDto;
import org.handler.dto.response.SupplierResponseDto;
import org.handler.exception.PromptNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AiServiceImplTest {

    @Mock
    private ChatClient.Builder builder;
    @Mock
    private ChatClient chatClient;
    @Mock
    private AiConfig aiConfig;
    @Mock
    private VectorStore vectorStore;

    @Test
    void generateQuestionsForRequestCase_ShouldReturnAiResponse_WhenPromptExists() {
        CaseRequestDto request = CaseRequestDto.builder()
                .parameters(Map.of("description", "Trash is not collected", "location", "Main street"))
                .build();

        ChatClient.ChatClientRequestSpec requestSpec = mock(ChatClient.ChatClientRequestSpec.class);
        ChatClient.CallResponseSpec callResponseSpec = mock(ChatClient.CallResponseSpec.class);

        when(builder.build()).thenReturn(chatClient);
        when(aiConfig.getPrompt("environment-case")).thenReturn(Optional.of("Prompt template:\n%s"));
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.content()).thenReturn("Please provide photo evidence.");

        AiServiceImpl service = new AiServiceImpl(builder, aiConfig, vectorStore);

        CompletableFuture<String> result = service.generateQuestionsForRequestCase(10L, request);

        assertEquals("Please provide photo evidence.", result.join());
        ArgumentCaptor<String> promptCaptor = ArgumentCaptor.forClass(String.class);
        verify(requestSpec).user(promptCaptor.capture());
        String finalPrompt = promptCaptor.getValue();
        assertTrue(finalPrompt.contains("Service request details:"));
        assertTrue(finalPrompt.contains("description"));
        assertTrue(finalPrompt.contains("location"));
    }

    @Test
    void generateQuestionsForRequestCase_ShouldThrowPromptNotFoundException_WhenPromptMissing() {
        CaseRequestDto request = CaseRequestDto.builder()
                .parameters(Map.of("description", "Issue"))
                .build();

        when(builder.build()).thenReturn(chatClient);
        when(aiConfig.getPrompt("environment-case")).thenReturn(Optional.empty());

        AiServiceImpl service = new AiServiceImpl(builder, aiConfig, vectorStore);

        assertThrows(PromptNotFoundException.class, () -> service.generateQuestionsForRequestCase(11L, request));

        verify(chatClient, never()).prompt();
    }

    @Test
    void generateQuestionsForRequestCase_ShouldThrowNullPointerException_WhenRequestIsNull() {
        when(builder.build()).thenReturn(chatClient);

        AiServiceImpl service = new AiServiceImpl(builder, aiConfig, vectorStore);

        assertThrows(NullPointerException.class, () -> service.generateQuestionsForRequestCase(13L, null));

        verifyNoInteractions(aiConfig);
    }

    @Test
    void generateSupplierSuggestionsRag_ShouldThrowPromptNotFoundException_WhenTemplateMissing() {
        Document doc = mock(Document.class);
        when(doc.getMetadata()).thenReturn(Map.of(
                "supplier_names", List.of("Alpha"),
                "buyer", "City",
                "cpv_codes", "45230000"
        ));
        when(doc.getText()).thenReturn("Work record");

        when(builder.build()).thenReturn(chatClient);
        when(vectorStore.similaritySearch(any(SearchRequest.class))).thenReturn(List.of(doc));
        when(aiConfig.getPrompt("supplier-suggestions")).thenReturn(Optional.empty());

        AiServiceImpl service = new AiServiceImpl(builder, aiConfig, vectorStore);

        assertThrows(PromptNotFoundException.class, () -> service.generateSupplierSuggestionsRag("query", "4523"));

        verify(chatClient, never()).prompt();
    }

    @Test
    void generateSupplierSuggestionsRag_ShouldThrowIllegalArgumentException_WhenQueryIsNull() {
        when(builder.build()).thenReturn(chatClient);

        AiServiceImpl service = new AiServiceImpl(builder, aiConfig, vectorStore);

        assertThrows(IllegalArgumentException.class, () -> service.generateSupplierSuggestionsRag(null, "4523"));
    }
}
