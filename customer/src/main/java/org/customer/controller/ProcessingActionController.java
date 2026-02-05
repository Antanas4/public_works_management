package org.customer.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.handler.dto.request.ProcessingActionRequestDto;
import org.handler.dto.response.ProcessingActionResponseDto;
import org.handler.service.ProcessingActionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/processing-actions")
@RequiredArgsConstructor
public class ProcessingActionController {
    private final ProcessingActionService processingActionService;

    @GetMapping
    public ResponseEntity<List<ProcessingActionResponseDto>> getAllProcessingActions() {
        List<ProcessingActionResponseDto> processingActionResponseDtos = processingActionService.getAllProcessingActions();
        return ResponseEntity.ok(processingActionResponseDtos);
    }

    @GetMapping("/{processingActionId}")
    public ResponseEntity<ProcessingActionResponseDto> getProcessingActionById(@PathVariable Long processingActionId) {
        ProcessingActionResponseDto processingActionResponseDto = processingActionService.getProcessingActionById(processingActionId);
        return ResponseEntity.ok(processingActionResponseDto);
    }

    @PutMapping("/{processingActionId}")
    public ResponseEntity<ProcessingActionResponseDto> updateProcessingAction(
            @PathVariable Long processingActionId,
            @Valid @RequestBody ProcessingActionRequestDto requestDto
    ) {
        ProcessingActionResponseDto response = processingActionService.updateProcessingAction(processingActionId, requestDto);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{processingActionId}")
    public ResponseEntity<String> deleteProcessingAction(@PathVariable Long processingActionId) {
        processingActionService.deleteProcessingAction(processingActionId);
        return ResponseEntity.ok("Processing action deleted successfully.");
    }
}
