package org.handler.service;

import org.handler.dto.request.ProcessingActionRequestDto;
import org.handler.dto.response.ProcessingActionResponseDto;

import java.util.List;

public interface ProcessingActionService {
    List<ProcessingActionResponseDto> getAllProcessingActions();

    ProcessingActionResponseDto getProcessingActionById(Long processingActionId);

    ProcessingActionResponseDto updateProcessingAction(Long processingActionId, ProcessingActionRequestDto processingActionRequestDto);

    void deleteProcessingAction(Long processingActionId);

    void createProcessingAction(ProcessingActionRequestDto processingActionRequestDto);
}
