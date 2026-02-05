package org.handler.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.handler.dto.request.CaseRequestDto;
import org.handler.dto.request.CommentRequestDto;
import org.handler.dto.request.PaginationRequest;
import org.handler.dto.response.CaseResponseDto;
import org.handler.dto.response.PaginationResponse;
import org.handler.exception.CaseNotFoundException;
import org.handler.exception.UserNotFoundException;
import org.handler.mapper.CaseMapper;
import org.handler.model.Case;
import org.handler.model.ProcessingAction;
import org.handler.model.User;
import org.handler.model.enums.CaseStatus;
import org.handler.model.enums.CaseType;
import org.handler.model.enums.ProcessingStatus;
import org.handler.repository.CaseRepository;
import org.handler.repository.UserRepository;
import org.handler.service.AiService;
import org.handler.service.CaseService;
import org.handler.service.CommentService;
import org.handler.specification.CaseSpecification;
import org.handler.utils.PaginationUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CaseServiceImpl implements CaseService {
    private final CaseRepository caseRepository;
    private final CaseMapper caseMapper;
    private final CommentService commentService;
    private final AiService aiService;
    private final UserRepository userRepository;

    @Override
    public CaseResponseDto createCase(CaseRequestDto caseRequestDto) {
        caseRequestDto.setUserId(1L); //delete after logic of getting user id is added!!

        User user = userRepository.findById(caseRequestDto.getUserId())
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + caseRequestDto.getUserId()));


        Case caseEntity = new Case();
        ProcessingAction processingAction = buildProcessingAction(caseRequestDto, caseEntity);
        caseMapper.toCase(caseRequestDto,
                caseEntity,
                user,
                List.of(processingAction));

        CaseStatus caseStatus = determineCaseStatusBasedOnType(caseRequestDto);
        caseEntity.setStatus(caseStatus);
        Case savedCase = caseRepository.save(caseEntity);

        generateQuestionsForRequestCase(caseRequestDto, savedCase);

        return caseMapper.toCaseResponseDto(savedCase);
    }

    @Override
    public CaseResponseDto getCaseById(Long caseId) {
        Case caseEntity = findCaseById(caseId);

        return caseMapper.toCaseResponseDto(caseEntity);
    }

    @Override
    public List<CaseResponseDto> getAllCases() {
        return caseRepository.findAll()
                .stream()
                .map(caseMapper::toCaseResponseDto)
                .toList();
    }

    @Override
    public void deleteCase(Long caseId) {
        Case caseEntity = findCaseById(caseId);

        caseRepository.delete(caseEntity);
    }

    @Override
    public Long getCaseCountByUserId(Long userId) {
        boolean exists = userRepository.existsById(userId);
        if (!exists) {
            throw new UserNotFoundException("User not found with ID: " + userId);
        }

        return caseRepository.countByUserId(userId);
    }

    @Override
    public PaginationResponse<CaseResponseDto> getCasesByUserId(Long userId,
                                                                PaginationRequest paginationRequest,
                                                                CaseStatus status,
                                                                CaseType type) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("User not found with ID: " + userId);
        }

        Pageable pageable = PaginationUtils.getPageable(paginationRequest);
        Specification<Case> specification = getSpecifications(userId, status, type);
        Page<Case> casePage = caseRepository.findAll(specification, pageable);

        List<CaseResponseDto> caseResponseDtos = casePage.stream()
                .map(caseMapper::toCaseResponseDto)
                .toList();

        return PaginationResponse.<CaseResponseDto>builder()
                .items(caseResponseDtos)
                .totalPages(casePage.getTotalPages())
                .totalElements(casePage.getTotalElements())
                .size(casePage.getSize())
                .pageNumber(casePage.getNumber())
                .build();
    }

    public Case findCaseById(Long caseId) {
        return caseRepository.findById(caseId)
                .orElseThrow(() -> new CaseNotFoundException("Case not found with ID: " + caseId));
    }


    private ProcessingAction buildProcessingAction(CaseRequestDto caseRequestDto, Case caseEntity) {
        return ProcessingAction.builder()
                .status(ProcessingStatus.DATA_PROVIDED)
                .parameters(caseRequestDto.getParameters())
                .caseRef(caseEntity)
                .build();
    }

    private void generateQuestionsForRequestCase(CaseRequestDto caseRequestDto, Case savedCase) {
        if (savedCase.getType() == CaseType.REQUEST) {
            aiService.generateQuestionsForRequestCase(savedCase.getId(), caseRequestDto)
                    .thenAccept(content -> {
                        CommentRequestDto commentRequestDto = CommentRequestDto.builder()
                                .userId(savedCase.getUser().getId())
                                .caseId(savedCase.getId())
                                .content(content)
                                .build();
                        commentService.addAiCommentResponse(commentRequestDto);
                    })
                    .exceptionally(ex -> {
                        log.error("Failed to generate AI question for case {}", savedCase.getId(), ex);
                        return null;
                    });
        }
    }

    private CaseStatus determineCaseStatusBasedOnType(CaseRequestDto caseRequestDto) {
        CaseType caseType = CaseType.valueOf(caseRequestDto.getType());

        if (caseType == CaseType.REQUEST) {
            return CaseStatus.WAITING_FOR_USER_RESPONSE;
        } else {
            return CaseStatus.OPEN;
        }
    }

    private Specification<Case> getSpecifications(Long userId, CaseStatus status, CaseType type) {
        return Specification
                .where(CaseSpecification.hasUserId(userId))
                .and(status != null ? CaseSpecification.hasStatus(status) : null)
                .and(type != null ? CaseSpecification.hasType(type) : null);
    }
}
