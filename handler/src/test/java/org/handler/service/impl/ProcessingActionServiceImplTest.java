package org.handler.service.impl;

import org.handler.dto.request.ProcessingActionRequestDto;
import org.handler.dto.response.ProcessingActionResponseDto;
import org.handler.exception.CaseNotFoundException;
import org.handler.exception.ProcessingActionNotFoundException;
import org.handler.mapper.ProcessingActionMapper;
import org.handler.model.Case;
import org.handler.model.ProcessingAction;
import org.handler.repository.CaseRepository;
import org.handler.repository.ProcessingActionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProcessingActionServiceImplTest {

    @Mock
    private ProcessingActionRepository processingActionRepository;
    @Mock
    private ProcessingActionMapper processingActionMapper;
    @Mock
    private CaseRepository caseRepository;

    @InjectMocks
    private ProcessingActionServiceImpl processingActionService;

    @Test
    void getAllProcessingActions_ShouldReturnMappedDtos_WhenRepositoryReturnsEntities() {
        ProcessingAction actionOne = new ProcessingAction();
        actionOne.setId(1L);
        ProcessingAction actionTwo = new ProcessingAction();
        actionTwo.setId(2L);

        ProcessingActionResponseDto dtoOne = ProcessingActionResponseDto.builder().id(1L).build();
        ProcessingActionResponseDto dtoTwo = ProcessingActionResponseDto.builder().id(2L).build();

        when(processingActionRepository.findAll()).thenReturn(List.of(actionOne, actionTwo));
        when(processingActionMapper.toProcessingActionResponseDto(actionOne)).thenReturn(dtoOne);
        when(processingActionMapper.toProcessingActionResponseDto(actionTwo)).thenReturn(dtoTwo);

        List<ProcessingActionResponseDto> result = processingActionService.getAllProcessingActions();

        assertEquals(2, result.size());
        assertSame(dtoOne, result.get(0));
        assertSame(dtoTwo, result.get(1));
        verify(processingActionRepository).findAll();
        verify(processingActionMapper).toProcessingActionResponseDto(actionOne);
        verify(processingActionMapper).toProcessingActionResponseDto(actionTwo);
    }


    @Test
    void getProcessingActionById_ShouldReturnMappedDto_WhenEntityExists() {
        ProcessingAction action = new ProcessingAction();
        action.setId(10L);
        ProcessingActionResponseDto responseDto = ProcessingActionResponseDto.builder().id(10L).build();

        when(processingActionRepository.findById(10L)).thenReturn(Optional.of(action));
        when(processingActionMapper.toProcessingActionResponseDto(action)).thenReturn(responseDto);

        ProcessingActionResponseDto result = processingActionService.getProcessingActionById(10L);

        assertSame(responseDto, result);
        verify(processingActionRepository).findById(10L);
        verify(processingActionMapper).toProcessingActionResponseDto(action);
    }

    @Test
    void getProcessingActionById_ShouldThrowProcessingActionNotFoundException_WhenEntityMissing() {
        when(processingActionRepository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(ProcessingActionNotFoundException.class, () -> processingActionService.getProcessingActionById(404L));

        verify(processingActionMapper, never()).toProcessingActionResponseDto(any());
    }


    @Test
    void updateProcessingAction_ShouldMapSaveAndReturnMappedDto_WhenInputsAreValid() {
        Long id = 20L;
        ProcessingActionRequestDto requestDto = ProcessingActionRequestDto.builder()
                .status("IN_PROGRESS")
                .caseId(5L)
                .parameters(Map.of("step", "review"))
                .build();

        ProcessingAction existing = new ProcessingAction();
        existing.setId(id);

        ProcessingAction saved = new ProcessingAction();
        saved.setId(id);

        ProcessingActionResponseDto expected = ProcessingActionResponseDto.builder().id(id).build();

        when(processingActionRepository.findById(id)).thenReturn(Optional.of(existing));
        when(processingActionRepository.save(existing)).thenReturn(saved);
        when(processingActionMapper.toProcessingActionResponseDto(saved)).thenReturn(expected);

        ProcessingActionResponseDto result = processingActionService.updateProcessingAction(id, requestDto);

        assertSame(expected, result);
        verify(processingActionMapper).toProcessingAction(requestDto, existing);
        verify(processingActionRepository).save(existing);
        verify(processingActionMapper).toProcessingActionResponseDto(saved);
    }

    @Test
    void updateProcessingAction_ShouldThrowProcessingActionNotFoundException_WhenEntityMissing() {
        when(processingActionRepository.findById(21L)).thenReturn(Optional.empty());

        assertThrows(
                ProcessingActionNotFoundException.class,
                () -> processingActionService.updateProcessingAction(21L, ProcessingActionRequestDto.builder().build())
        );

        verify(processingActionRepository, never()).save(any());
    }


    @Test
    void deleteProcessingAction_ShouldDeleteEntity_WhenEntityExists() {
        ProcessingAction existing = new ProcessingAction();
        existing.setId(30L);
        when(processingActionRepository.findById(30L)).thenReturn(Optional.of(existing));

        processingActionService.deleteProcessingAction(30L);

        verify(processingActionRepository).delete(existing);
    }

    @Test
    void deleteProcessingAction_ShouldThrowProcessingActionNotFoundException_WhenEntityMissing() {
        when(processingActionRepository.findById(31L)).thenReturn(Optional.empty());

        assertThrows(ProcessingActionNotFoundException.class, () -> processingActionService.deleteProcessingAction(31L));

        verify(processingActionRepository, never()).delete(any());
    }

    @Test
    void createProcessingAction_ShouldMapSetCaseAndSave_WhenCaseExists() {
        ProcessingActionRequestDto requestDto = ProcessingActionRequestDto.builder()
                .status("IN_PROGRESS")
                .caseId(40L)
                .parameters(Map.of("actionType", "ADD_COMMENT"))
                .build();

        Case caseRef = new Case();
        caseRef.setId(40L);

        when(caseRepository.findById(40L)).thenReturn(Optional.of(caseRef));

        processingActionService.createProcessingAction(requestDto);

        ArgumentCaptor<ProcessingAction> actionCaptor = ArgumentCaptor.forClass(ProcessingAction.class);
        verify(processingActionMapper).toProcessingAction(eq(requestDto), any(ProcessingAction.class));
        verify(processingActionRepository).save(actionCaptor.capture());

        ProcessingAction persisted = actionCaptor.getValue();
        assertSame(caseRef, persisted.getCaseRef());
    }

    @Test
    void createProcessingAction_ShouldThrowCaseNotFoundException_WhenCaseMissing() {
        ProcessingActionRequestDto requestDto = ProcessingActionRequestDto.builder().caseId(41L).build();
        when(caseRepository.findById(41L)).thenReturn(Optional.empty());

        assertThrows(CaseNotFoundException.class, () -> processingActionService.createProcessingAction(requestDto));

        verify(processingActionMapper, never()).toProcessingAction(any(), any());
        verify(processingActionRepository, never()).save(any());
    }

    @Test
    void createProcessingAction_ShouldThrowCaseNotFoundException_WhenCaseIdIsNull() {
        ProcessingActionRequestDto requestDto = ProcessingActionRequestDto.builder().caseId(null).build();
        when(caseRepository.findById(null)).thenReturn(Optional.empty());

        assertThrows(CaseNotFoundException.class, () -> processingActionService.createProcessingAction(requestDto));

        verify(processingActionRepository, never()).save(any());
    }
}
