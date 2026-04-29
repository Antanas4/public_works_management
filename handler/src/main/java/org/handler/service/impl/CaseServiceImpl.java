package org.handler.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.handler.dto.request.CaseRequestDto;
import org.handler.dto.request.CommentRequestDto;
import org.handler.dto.request.PaginationRequest;
import org.handler.dto.response.CaseResponseDto;
import org.handler.dto.response.PaginationResponse;
import org.handler.dto.response.SupplierResponseDto;
import org.handler.exception.CaseNotFoundException;
import org.handler.exception.CaseStatusUpdateNotAllowedException;
import org.handler.exception.ProcessingActionNotFoundException;
import org.handler.exception.SupplierAlreadyAssignedException;
import org.handler.mapper.CaseMapper;
import org.handler.mapper.SupplierMapper;
import org.handler.model.*;
import org.handler.model.enums.*;
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

import java.nio.file.Paths;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class CaseServiceImpl implements CaseService {
    private final CaseRepository caseRepository;
    private final CaseMapper caseMapper;
    private final CommentService commentService;
    private final AiService aiService;
    private final MinioService minioService;
    private final SupplierService supplierService;
    private final SupplierMapper supplierMapper;

    @Override
    public CaseResponseDto createCase(CaseRequestDto caseRequestDto, List<MultipartFile> photos) {
        User user = SecurityUtil.getCurrentUser();
        Case caseEntity = new Case();

        ProcessingAction processingAction = buildProcessingActionDataProvided(caseRequestDto, caseEntity);

        caseMapper.toCase(
                caseRequestDto,
                caseEntity,
                user,
                new ArrayList<>(List.of(processingAction))
        );

        caseEntity.setStatus(determineCaseStatusBasedOnType(caseRequestDto));
        caseEntity.setCpvCode(caseEntity.getSubtype().getCpvCode());

        Case savedCase = caseRepository.save(caseEntity);

        uploadCasePhotos(savedCase, photos);

        generateQuestionsForEnvironmentCase(caseRequestDto, savedCase);

        return caseMapper.toCaseResponseDto(savedCase);
    }

    @Override
    public CaseResponseDto getCaseById(Long caseId) {
        Case caseEntity = findCaseByIdWithPhotos(caseId);

        caseEntity.getPhotos().forEach(photo ->
                photo.setFilePath(minioService.getPresignedUrl(photo.getFilePath()))
        );

        return caseMapper.toCaseResponseDto(caseEntity);
    }

    @Override
    public PaginationResponse<CaseResponseDto> getAllCases(PaginationRequest paginationRequest,
                                                           CaseStatus status,
                                                           CaseType type) {
        Pageable pageable = PaginationUtils.getPageable(paginationRequest);
        Specification<Case> specification = Specification
                .where(status != null ? CaseSpecification.hasStatus(status) : null)
                .and(type != null ? CaseSpecification.hasType(type) : null);
        Page<Case> casePage = caseRepository.findAll(specification, pageable);

        List<CaseResponseDto> caseDtos = casePage.stream()
                .map(caseMapper::toCaseResponseDto)
                .toList();

        return PaginationResponse.<CaseResponseDto>builder()
                .items(caseDtos)
                .totalPages(casePage.getTotalPages())
                .totalElements(casePage.getTotalElements())
                .size(casePage.getSize())
                .pageNumber(casePage.getNumber())
                .build();
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
    public List<SupplierResponseDto> suggestSuppliersForCase(Long caseId) {
        Case caseEntity = findCaseById(caseId);
        ProcessingAction dataProvidedAction = getDataProvidedProcessingAction(caseEntity); //tikrinti
        String description = dataProvidedAction.getParameters().get("description");
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

    @Transactional
    @Override
    public void assignSupplier(Long caseId, Long supplierId) {
        Supplier supplier = supplierService.findSupplierById(supplierId);
        Case caseEntity = findCaseById(caseId);

        assertSupplierNotAlreadyAssigned(supplierId, caseEntity);

        caseEntity.setSupplier(supplier);
        ProcessingAction processingAction = buildProcessingActionSupplierAssigned(caseEntity, supplier);
        caseEntity.getProcessingActions().add(processingAction);
        caseEntity.setStatus(CaseStatus.IN_PROCESSING);

        supplier.getHandledCaseSubtypes().add(caseEntity.getSubtype());

        caseRepository.save(caseEntity);
    }

    @Override
    public Case findCaseByIdWithPhotos(Long caseId) {
        return caseRepository.findByIdWithPhotos(caseId)
                .orElseThrow(() -> new CaseNotFoundException("Case not found with ID: " + caseId));
    }

    @Override
    public Case findCaseById(Long caseId) {
        return caseRepository.findById(caseId)
                .orElseThrow(() -> new CaseNotFoundException("Case not found with ID: " + caseId));
    }

    @Override
    public CaseResponseDto updateCase(Long caseId, CaseRequestDto caseRequestDto) {
        Case caseEntity = findCaseById(caseId);
        User currentUser = SecurityUtil.getCurrentUser();

        caseMapper.toCase(
                caseRequestDto,
                caseEntity,
                currentUser,
                caseEntity.getProcessingActions()
        );

        if (caseRequestDto.getStatus() != null) {
            caseEntity.setStatus(CaseStatus.valueOf(caseRequestDto.getStatus()));
        }

        ProcessingAction processingAction = buildProcessingActionCaseUpdated(caseRequestDto, caseEntity);
        caseEntity.getProcessingActions().add(processingAction);

        Case updatedCase = caseRepository.save(caseEntity);

        return caseMapper.toCaseResponseDto(updatedCase);
    }

    @Override
    public void updateStatus(Long caseId, CaseStatus status) {
        Case caseEntity = findCaseById(caseId);

        if (status == CaseStatus.IN_SUPPLIER_PROCESSING && caseEntity.getSupplier() == null) {
            throw new CaseStatusUpdateNotAllowedException(
                    "Case status cannot be changed to IN_SUPPLIER_PROCESSING because no supplier is assigned."
            );
        }

        caseEntity.setStatus(status);
        caseRepository.save(caseEntity);
    }

    private void assertSupplierNotAlreadyAssigned(Long supplierId, Case caseEntity) {
        if (caseEntity.getSupplier() != null && caseEntity.getSupplier().getId().equals(supplierId)) {
            throw new SupplierAlreadyAssignedException(
                    "Supplier already assigned to this case",
                    supplierMapper.toResponseDto(caseEntity.getSupplier())
            );
        }
    }

    private ProcessingAction getDataProvidedProcessingAction(Case caseEntity) {
        return caseEntity.getProcessingActions()
                .stream()
                .filter(action -> action.getStatus() == ProcessingStatus.DATA_PROVIDED)
                .min(Comparator.comparing(ProcessingAction::getCreatedAt))
                .orElseThrow(() ->
                        new ProcessingActionNotFoundException(
                                "Initial processing action not found for case"
                        ));
    }

    private ProcessingAction buildProcessingActionCaseUpdated(CaseRequestDto caseRequestDto, Case caseEntity) {
        ProcessingStatus processingStatus =
                caseEntity.getStatus() == CaseStatus.CLOSED
                        ? ProcessingStatus.COMPLETED
                        : ProcessingStatus.IN_PROGRESS;

        return ProcessingAction.builder()
                .status(processingStatus)
                .parameters(caseRequestDto.getParameters())
                .caseRef(caseEntity)
                .build();
    }

    private ProcessingAction buildProcessingActionDataProvided(CaseRequestDto caseRequestDto, Case caseEntity) {
        return ProcessingAction.builder()
                .status(ProcessingStatus.DATA_PROVIDED)
                .parameters(caseRequestDto.getParameters())
                .caseRef(caseEntity)
                .build();
    }

    private ProcessingAction buildProcessingActionSupplierAssigned(Case caseEntity, Supplier supplier) {
        Map<String, String> parameters = new HashMap<>();

        parameters.put("supplierName", supplier.getName());

        return ProcessingAction.builder()
                .status(ProcessingStatus.IN_PROGRESS)
                .parameters(parameters)
                .caseRef(caseEntity)
                .build();
    }


    private void generateQuestionsForEnvironmentCase(CaseRequestDto caseRequestDto, Case savedCase) {
        if (savedCase.getType() == CaseType.ENVIRONMENT) {
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

        if (caseType == CaseType.ENVIRONMENT) {
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
            String fileName = Paths.get(photoUrl).getFileName().toString();

            casePhoto.setFileName(fileName);
            casePhoto.setFilePath(photoUrl);
            casePhoto.setCaseRef(savedCase);

            savedCase.getPhotos().add(casePhoto);
        }

        caseRepository.save(savedCase);
    }
}
