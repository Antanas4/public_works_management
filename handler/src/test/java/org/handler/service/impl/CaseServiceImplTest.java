package org.handler.service.impl;

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
import org.handler.model.Case;
import org.handler.model.CasePhoto;
import org.handler.model.ProcessingAction;
import org.handler.model.Supplier;
import org.handler.model.User;
import org.handler.model.UserPrincipal;
import org.handler.model.enums.CaseStatus;
import org.handler.model.enums.CaseSubtype;
import org.handler.model.enums.CaseType;
import org.handler.model.enums.ProcessingStatus;
import org.handler.model.enums.SupplierSource;
import org.handler.model.enums.UserType;
import org.handler.repository.CaseRepository;
import org.handler.service.AiService;
import org.handler.service.CommentService;
import org.handler.service.MinioService;
import org.handler.service.SupplierService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CaseServiceImplTest {

    @Mock
    private CaseRepository caseRepository;
    @Mock
    private CaseMapper caseMapper;
    @Mock
    private CommentService commentService;
    @Mock
    private AiService aiService;
    @Mock
    private MinioService minioService;
    @Mock
    private SupplierService supplierService;
    @Mock
    private SupplierMapper supplierMapper;

    @InjectMocks
    private CaseServiceImpl caseService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createCase_ShouldCreateCaseWithPhotosAndOpenStatus_WhenNonEnvironmentCase() {
        User currentUser = createUser(10L);
        setCurrentUser(currentUser);

        CaseRequestDto request = CaseRequestDto.builder()
                .type(CaseType.SURFACE_REPAIR.name())
                .subtype(CaseSubtype.ROAD_REPAIR.name())
                .title("Road is broken")
                .parameters(Map.of("description", "Large pothole near school"))
                .build();

        CaseResponseDto expectedResponse = CaseResponseDto.builder().id(100L).build();
        MultipartFile photo = mock(MultipartFile.class);
        List<MultipartFile> photos = List.of(photo);

        doAnswer(invocation -> {
            CaseRequestDto dto = invocation.getArgument(0);
            Case caseEntity = invocation.getArgument(1);
            User user = invocation.getArgument(2);
            caseEntity.setType(CaseType.valueOf(dto.getType()));
            caseEntity.setSubtype(CaseSubtype.valueOf(dto.getSubtype()));
            caseEntity.setTitle(dto.getTitle());
            caseEntity.setUser(user);
            return null;
        }).when(caseMapper).toCase(any(), any(), any(), anyList());

        when(caseRepository.save(any(Case.class))).thenAnswer(invocation -> {
            Case caseEntity = invocation.getArgument(0);
            if (caseEntity.getId() == null) {
                caseEntity.setId(100L);
            }
            return caseEntity;
        });
        when(minioService.uploadPhotos(photos, 100L)).thenReturn(List.of("bucket/cases/100/photo-1.jpg"));
        when(caseMapper.toCaseResponseDto(any(Case.class))).thenReturn(expectedResponse);

        CaseResponseDto actual = caseService.createCase(request, photos);

        assertSame(expectedResponse, actual);
        verify(caseMapper).toCase(eq(request), any(Case.class), eq(currentUser), anyList());
        verify(caseRepository, times(2)).save(any(Case.class));
        verify(minioService).uploadPhotos(photos, 100L);
        verify(aiService, never()).generateQuestionsForRequestCase(anyLong(), any());
    }

    @Test
    void createCase_ShouldGenerateAiComment_WhenEnvironmentCase() {
        User currentUser = createUser(11L);
        setCurrentUser(currentUser);

        CaseRequestDto request = CaseRequestDto.builder()
                .type(CaseType.ENVIRONMENT.name())
                .subtype(CaseSubtype.WASTE_MANAGEMENT.name())
                .title("Overflowing trash bins")
                .parameters(Map.of("description", "Trash bins are full for days"))
                .build();

        doAnswer(invocation -> {
            CaseRequestDto dto = invocation.getArgument(0);
            Case caseEntity = invocation.getArgument(1);
            User user = invocation.getArgument(2);
            caseEntity.setType(CaseType.valueOf(dto.getType()));
            caseEntity.setSubtype(CaseSubtype.valueOf(dto.getSubtype()));
            caseEntity.setTitle(dto.getTitle());
            caseEntity.setUser(user);
            return null;
        }).when(caseMapper).toCase(any(), any(), any(), anyList());

        when(caseRepository.save(any(Case.class))).thenAnswer(invocation -> {
            Case caseEntity = invocation.getArgument(0);
            if (caseEntity.getId() == null) {
                caseEntity.setId(200L);
            }
            return caseEntity;
        });
        when(aiService.generateQuestionsForRequestCase(200L, request))
                .thenReturn(CompletableFuture.completedFuture("Please provide exact location."));
        when(caseMapper.toCaseResponseDto(any(Case.class))).thenReturn(CaseResponseDto.builder().id(200L).build());

        caseService.createCase(request, null);

        ArgumentCaptor<CommentRequestDto> commentCaptor = ArgumentCaptor.forClass(CommentRequestDto.class);
        verify(commentService).addAiCommentResponse(commentCaptor.capture());
        CommentRequestDto aiCommentRequest = commentCaptor.getValue();

        assertEquals(11L, aiCommentRequest.getUserId());
        assertEquals(200L, aiCommentRequest.getCaseId());
        assertEquals("Please provide exact location.", aiCommentRequest.getContent());
    }

    @Test
    void createCase_ShouldThrowNullPointerException_WhenRequestIsNull() {
        assertThrows(NullPointerException.class, () -> caseService.createCase(null, List.of()));

        verifyNoInteractions(caseRepository, caseMapper, minioService, aiService, commentService);
    }

    @Test
    void getCaseById_ShouldReturnCaseWithPresignedPhotoUrls_WhenCaseExists() {
        Case caseEntity = new Case();
        caseEntity.setId(1L);
        CasePhoto photo = new CasePhoto();
        photo.setFilePath("bucket/cases/1/photo.jpg");
        caseEntity.setPhotos(new ArrayList<>(List.of(photo)));

        CaseResponseDto expectedResponse = CaseResponseDto.builder().id(1L).build();

        when(caseRepository.findByIdWithPhotos(1L)).thenReturn(Optional.of(caseEntity));
        when(minioService.getPresignedUrl("bucket/cases/1/photo.jpg")).thenReturn("https://signed-url/photo.jpg");
        when(caseMapper.toCaseResponseDto(caseEntity)).thenReturn(expectedResponse);

        CaseResponseDto actual = caseService.getCaseById(1L);

        assertSame(expectedResponse, actual);
        assertEquals("https://signed-url/photo.jpg", caseEntity.getPhotos().getFirst().getFilePath());
        verify(minioService).getPresignedUrl("bucket/cases/1/photo.jpg");
        verify(caseMapper).toCaseResponseDto(caseEntity);
    }

    @Test
    void getCaseById_ShouldThrowCaseNotFoundException_WhenCaseDoesNotExist() {
        when(caseRepository.findByIdWithPhotos(99L)).thenReturn(Optional.empty());

        assertThrows(CaseNotFoundException.class, () -> caseService.getCaseById(99L));

        verify(caseMapper, never()).toCaseResponseDto(any());
    }

    @Test
    void getAllCases_ShouldReturnPaginatedResponse_WhenRepositoryReturnsData() {
        PaginationRequest paginationRequest = PaginationRequest.builder().page(0).size(2).build();
        Case caseOne = new Case();
        caseOne.setId(1L);
        Case caseTwo = new Case();
        caseTwo.setId(2L);

        Page<Case> page = new PageImpl<>(List.of(caseOne, caseTwo), PageRequest.of(0, 2), 2);
        when(caseRepository.findAll(ArgumentMatchers.<Specification<Case>>any(), any(Pageable.class))).thenReturn(page);
        when(caseMapper.toCaseResponseDto(caseOne)).thenReturn(CaseResponseDto.builder().id(1L).build());
        when(caseMapper.toCaseResponseDto(caseTwo)).thenReturn(CaseResponseDto.builder().id(2L).build());

        PaginationResponse<CaseResponseDto> response = caseService.getAllCases(
                paginationRequest,
                CaseStatus.OPEN,
                CaseType.ENVIRONMENT
        );

        assertEquals(2, response.getItems().size());
        assertEquals(1, response.getTotalPages());
        assertEquals(2, response.getTotalElements());
        verify(caseRepository).findAll(ArgumentMatchers.<Specification<Case>>any(), any(Pageable.class));
        verify(caseMapper).toCaseResponseDto(caseOne);
        verify(caseMapper).toCaseResponseDto(caseTwo);
    }

    @Test
    void deleteCase_ShouldDeleteCase_WhenCaseExists() {
        Case caseEntity = new Case();
        caseEntity.setId(50L);
        when(caseRepository.findById(50L)).thenReturn(Optional.of(caseEntity));

        caseService.deleteCase(50L);

        verify(caseRepository).delete(caseEntity);
    }

    @Test
    void getCaseCountForCurrentUser_ShouldReturnRepositoryCount() {
        setCurrentUser(createUser(333L));
        when(caseRepository.countByUserId(333L)).thenReturn(9L);

        Long count = caseService.getCaseCountForCurrentUser();

        assertEquals(9L, count);
        verify(caseRepository).countByUserId(333L);
    }

    @Test
    void getUserCases_ShouldReturnPaginatedResponseForCurrentUser() {
        setCurrentUser(createUser(777L));

        PaginationRequest paginationRequest = PaginationRequest.builder().page(0).size(1).build();
        Case caseEntity = new Case();
        caseEntity.setId(91L);
        Page<Case> page = new PageImpl<>(List.of(caseEntity), PageRequest.of(0, 1), 1);

        when(caseRepository.findAll(ArgumentMatchers.<Specification<Case>>any(), any(Pageable.class))).thenReturn(page);
        when(caseMapper.toCaseResponseDto(caseEntity)).thenReturn(CaseResponseDto.builder().id(91L).build());

        PaginationResponse<CaseResponseDto> response = caseService.getUserCases(
                paginationRequest,
                CaseStatus.OPEN,
                CaseType.SURFACE_REPAIR
        );

        assertEquals(1, response.getItems().size());
        assertEquals(1L, response.getTotalElements());
        verify(caseRepository).findAll(ArgumentMatchers.<Specification<Case>>any(), any(Pageable.class));
        verify(caseMapper).toCaseResponseDto(caseEntity);
    }

    @Test
    void suggestSuppliersForCase_ShouldReturnSuggestions_WhenDataProvidedActionExists() {
        Case caseEntity = new Case();
        caseEntity.setId(401L);
        caseEntity.setTitle("Broken road signs");
        caseEntity.setCpvCode("45233290");

        ProcessingAction initialAction = ProcessingAction.builder()
                .status(ProcessingStatus.DATA_PROVIDED)
                .parameters(Map.of("description", "Several signs are missing"))
                .createdAt(LocalDateTime.now())
                .build();
        caseEntity.setProcessingActions(new ArrayList<>(List.of(initialAction)));

        List<SupplierResponseDto> suggestions = List.of(new SupplierResponseDto());
        when(caseRepository.findById(401L)).thenReturn(Optional.of(caseEntity));
        when(aiService.generateSupplierSuggestionsRag(anyString(), eq("4523"))).thenReturn(suggestions);

        List<SupplierResponseDto> response = caseService.suggestSuppliersForCase(401L);

        assertSame(suggestions, response);
        ArgumentCaptor<String> queryCaptor = ArgumentCaptor.forClass(String.class);
        verify(aiService).generateSupplierSuggestionsRag(queryCaptor.capture(), eq("4523"));
        String query = queryCaptor.getValue();
        assertTrue(query.contains("CPV: 4523"));
        assertTrue(query.contains("Title: Broken road signs"));
        assertTrue(query.contains("Description: Several signs are missing"));
    }

    @Test
    void suggestSuppliersForCase_ShouldThrowProcessingActionNotFoundException_WhenNoDataProvidedAction() {
        Case caseEntity = new Case();
        caseEntity.setId(402L);
        caseEntity.setCpvCode("45233290");
        caseEntity.setTitle("Missing data");
        caseEntity.setProcessingActions(new ArrayList<>());

        when(caseRepository.findById(402L)).thenReturn(Optional.of(caseEntity));

        assertThrows(ProcessingActionNotFoundException.class, () -> caseService.suggestSuppliersForCase(402L));

        verify(aiService, never()).generateSupplierSuggestionsRag(anyString(), anyString());
    }

    @Test
    void suggestSuppliersForCase_ShouldPropagateException_WhenAiServiceFails() {
        Case caseEntity = new Case();
        caseEntity.setId(403L);
        caseEntity.setTitle("Street cleaning issue");
        caseEntity.setCpvCode("90610000");

        ProcessingAction initialAction = ProcessingAction.builder()
                .status(ProcessingStatus.DATA_PROVIDED)
                .parameters(Map.of("description", "Area not cleaned for week"))
                .createdAt(LocalDateTime.now())
                .build();
        caseEntity.setProcessingActions(new ArrayList<>(List.of(initialAction)));

        RuntimeException aiFailure = new RuntimeException("AI error");
        when(caseRepository.findById(403L)).thenReturn(Optional.of(caseEntity));
        when(aiService.generateSupplierSuggestionsRag(anyString(), eq("9061"))).thenThrow(aiFailure);

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> caseService.suggestSuppliersForCase(403L));

        assertSame(aiFailure, thrown);
    }

    @Test
    void assignSupplier_ShouldAssignSupplierAndSaveCase_WhenSupplierNotAssignedYet() {
        Supplier supplier = Supplier.builder()
                .id(21L)
                .name("City Works Ltd")
                .source(SupplierSource.AI)
                .handledCaseSubtypes(new HashSet<>())
                .build();

        Case caseEntity = new Case();
        caseEntity.setId(700L);
        caseEntity.setSubtype(CaseSubtype.ROAD_REPAIR);
        caseEntity.setProcessingActions(new ArrayList<>());

        when(supplierService.findSupplierById(21L)).thenReturn(supplier);
        when(caseRepository.findById(700L)).thenReturn(Optional.of(caseEntity));

        caseService.assignSupplier(700L, 21L);

        assertEquals(CaseStatus.IN_PROCESSING, caseEntity.getStatus());
        assertSame(supplier, caseEntity.getSupplier());
        assertEquals(1, caseEntity.getProcessingActions().size());
        assertTrue(supplier.getHandledCaseSubtypes().contains(CaseSubtype.ROAD_REPAIR));
        verify(caseRepository).save(caseEntity);
    }

    @Test
    void assignSupplier_ShouldThrowSupplierAlreadyAssignedException_WhenSameSupplierAlreadyAssigned() {
        Supplier supplier = Supplier.builder().id(30L).name("Same Supplier").source(SupplierSource.AI).build();
        Case caseEntity = new Case();
        caseEntity.setId(701L);
        caseEntity.setSupplier(supplier);
        caseEntity.setProcessingActions(new ArrayList<>());

        SupplierResponseDto responseDto = new SupplierResponseDto();
        responseDto.setId(30L);

        when(supplierService.findSupplierById(30L)).thenReturn(supplier);
        when(caseRepository.findById(701L)).thenReturn(Optional.of(caseEntity));
        when(supplierMapper.toResponseDto(supplier)).thenReturn(responseDto);

        assertThrows(SupplierAlreadyAssignedException.class, () -> caseService.assignSupplier(701L, 30L));

        verify(caseRepository, never()).save(any());
    }


    @Test
    void updateStatus_ShouldThrowCaseStatusUpdateNotAllowedException_WhenSupplierIsMissing() {
        Case caseEntity = new Case();
        caseEntity.setId(901L);
        caseEntity.setSupplier(null);
        when(caseRepository.findById(901L)).thenReturn(Optional.of(caseEntity));

        assertThrows(
                CaseStatusUpdateNotAllowedException.class,
                () -> caseService.updateStatus(901L, CaseStatus.IN_SUPPLIER_PROCESSING)
        );

        verify(caseRepository, never()).save(any());
    }

    @Test
    void updateStatus_ShouldSaveCase_WhenTransitionIsAllowed() {
        Case caseEntity = new Case();
        caseEntity.setId(902L);
        caseEntity.setStatus(CaseStatus.OPEN);
        when(caseRepository.findById(902L)).thenReturn(Optional.of(caseEntity));

        caseService.updateStatus(902L, CaseStatus.READY_FOR_REVIEW);

        assertEquals(CaseStatus.READY_FOR_REVIEW, caseEntity.getStatus());
        verify(caseRepository).save(caseEntity);
    }

    @Test
    void findCaseById_ShouldThrowCaseNotFoundException_WhenCaseIdDoesNotExist() {
        when(caseRepository.findById(4040L)).thenReturn(Optional.empty());

        assertThrows(CaseNotFoundException.class, () -> caseService.findCaseById(4040L));
    }

    private void setCurrentUser(User user) {
        UserPrincipal principal = new UserPrincipal(user);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private User createUser(Long id) {
        return User.builder()
                .id(id)
                .username("user" + id)
                .password("secret")
                .email("user" + id + "@test.com")
                .type(UserType.CLIENT)
                .build();
    }
}
