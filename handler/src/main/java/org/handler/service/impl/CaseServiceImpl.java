package org.handler.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.handler.config.AiConfig;
import org.handler.dto.request.CaseRequestDto;
import org.handler.dto.request.CommentRequestDto;
import org.handler.dto.request.PaginationRequest;
import org.handler.dto.response.CaseResponseDto;
import org.handler.dto.response.PaginationResponse;
import org.handler.dto.response.SupplierDto;
import org.handler.exception.CaseNotFoundException;
import org.handler.exception.ProcessingActionNotFoundException;
import org.handler.exception.PromptNotFoundException;
import org.handler.mapper.CaseMapper;
import org.handler.model.Case;
import org.handler.model.CasePhoto;
import org.handler.model.ProcessingAction;
import org.handler.model.User;
import org.handler.model.enums.CaseStatus;
import org.handler.model.enums.CaseType;
import org.handler.model.enums.ProcessingStatus;
import org.handler.repository.CaseRepository;
import org.handler.service.*;
import org.handler.specification.CaseSpecification;
import org.handler.utils.PaginationUtils;
import org.handler.utils.SecurityUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CaseServiceImpl implements CaseService {
    private static final String DOCUMENT_RETRIEVAL_REQUEST = "document-retrieval-request";
    private final CaseRepository caseRepository;
    private final CaseMapper caseMapper;
    private final CommentService commentService;
    private final AiService aiService;
    private final MinioService minioService;
    private final CpvService cpvService;
    private final AiConfig aiConfig;

    @Override
    public CaseResponseDto createCase(CaseRequestDto caseRequestDto, List<MultipartFile> photos) {
        User user = SecurityUtil.getCurrentUser();
        Case caseEntity = new Case();
        String caseCpv = cpvService.getCpvBySubtype(caseRequestDto.getSubtype());

        ProcessingAction processingAction = buildProcessingAction(caseRequestDto, caseEntity);

        caseMapper.toCase(
                caseRequestDto,
                caseEntity,
                user,
                new ArrayList<>(List.of(processingAction))
        );

        CaseStatus caseStatus = determineCaseStatusBasedOnType(caseRequestDto);
        caseEntity.setStatus(caseStatus);
        caseEntity.setCpvCode(caseCpv);

        Case savedCase = caseRepository.save(caseEntity);

        uploadCasePhotos(savedCase, photos);

        generateQuestionsForRequestCase(caseRequestDto, savedCase);

        return caseMapper.toCaseResponseDto(savedCase);
    }

    @Override
    public CaseResponseDto getCaseById(Long caseId) {
        Case caseEntity = findCaseById(caseId);
        Long currentUserId = SecurityUtil.getCurrentUserId();

        if (!caseEntity.getUser().getId().equals(currentUserId)) {
            throw new CaseNotFoundException("Case not found");
        }

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
    public Long getCaseCountForCurrentUser() {
        Long userId = SecurityUtil.getCurrentUserId();
        return caseRepository.countByUserId(userId);
    }

    @Override
    public PaginationResponse<CaseResponseDto> getUserCases(PaginationRequest paginationRequest,
                                                            CaseStatus status,
                                                            CaseType type) {

        Long userId = SecurityUtil.getCurrentUserId();
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

    @Override
    public List<SupplierDto> suggestCompaniesForCase(Long caseId) {
        Case caseEntity = caseRepository.findById(caseId)
                .orElseThrow(() ->
                        new CaseNotFoundException("Case not found"));

        ProcessingAction latestAction = getLatestProcessingAction(caseEntity);
        String description = latestAction.getParameters().get("description");
        String cpvCode = caseEntity.getCpvCode();
        String cpvPrefix = cpvCode.substring(0, 4);
        String query = String.format(
                "CPV: %s\nTitle: %s\nDescription: %s",
                cpvPrefix,
                caseEntity.getTitle(),
                description
        );

        try {
            return aiService.generateSupplierSuggestionsRag(query, cpvPrefix);
        } catch (Exception ex) {
            log.error("Supplier suggestion generation failed", ex);
            throw ex;
        }
    }

    private ProcessingAction getLatestProcessingAction(Case caseEntity) {
        return caseEntity.getProcessingActions()
                .stream()
                .max(Comparator.comparing(ProcessingAction::getCreatedAt))
                .orElseThrow(() ->
                        new ProcessingActionNotFoundException(
                                "Processing action not found for case"
                        ));
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

    private void uploadCasePhotos(Case savedCase, List<MultipartFile> photos) {
        if (photos == null || photos.isEmpty()) {
            return;
        }

        List<String> photoUrls = minioService.uploadPhotos(photos, savedCase.getId());

        for (String photoUrl : photoUrls) {
            CasePhoto casePhoto = new CasePhoto();

            URI uri = URI.create(photoUrl);
            String fileName = Paths.get(uri.getPath()).getFileName().toString();

            casePhoto.setFileName(fileName);
            casePhoto.setFilePath(photoUrl);
            casePhoto.setCaseRef(savedCase);

            savedCase.getPhotos().add(casePhoto);
        }
    }
}
