package org.handler.service.impl;

import lombok.RequiredArgsConstructor;
import org.handler.dto.request.ProcessingActionRequestDto;
import org.handler.dto.response.ProcessingActionResponseDto;
import org.handler.exception.CaseNotFoundException;
import org.handler.exception.ProcessingActionNotFoundException;
import org.handler.mapper.ProcessingActionMapper;
import org.handler.model.Case;
import org.handler.model.ProcessingAction;
import org.handler.repository.CaseRepository;
import org.handler.repository.ProcessingActionRepository;
import org.handler.service.ProcessingActionService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProcessingActionServiceImpl implements ProcessingActionService {
    private final ProcessingActionRepository processingActionRepository;
    private final ProcessingActionMapper processingActionMapper;
    private final CaseRepository caseRepository;

    @Override
    public List<ProcessingActionResponseDto> getAllProcessingActions() {
        return processingActionRepository.findAll()
                .stream()
                .map(processingActionMapper::toProcessingActionResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public ProcessingActionResponseDto getProcessingActionById(Long processingActionId) {
        ProcessingAction processingAction = findProcessingActionById(processingActionId);

        return processingActionMapper.toProcessingActionResponseDto(processingAction);
    }

    @Override
    public ProcessingActionResponseDto updateProcessingAction(Long processingActionId, ProcessingActionRequestDto processingActionRequestDto) {
        ProcessingAction processingAction = findProcessingActionById(processingActionId);

        processingActionMapper.toProcessingAction(processingActionRequestDto, processingAction);

        return processingActionMapper.toProcessingActionResponseDto(
                processingActionRepository.save(processingAction)
        );
    }

    @Override
    public void deleteProcessingAction(Long processingActionId) {
        ProcessingAction processingAction = findProcessingActionById(processingActionId);

        processingActionRepository.delete(processingAction);
    }

    @Override
    public void createProcessingAction(ProcessingActionRequestDto processingActionRequestDto) {
        Case caseRef = caseRepository.findById(processingActionRequestDto.getCaseId()).orElseThrow(
                () -> new CaseNotFoundException("Case not found")
        );
        ProcessingAction processingAction = new ProcessingAction();

        processingActionMapper.toProcessingAction(processingActionRequestDto, processingAction);
        processingAction.setCaseRef(caseRef);

        processingActionRepository.save(processingAction);
    }

    private ProcessingAction findProcessingActionById(Long processingActionId) {
        return processingActionRepository.findById(processingActionId)
                .orElseThrow(() -> new ProcessingActionNotFoundException("ProcessingAction not found with id: " + processingActionId));
    }
}
