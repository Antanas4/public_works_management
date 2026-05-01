package org.handler.service.impl;

import org.handler.dto.request.CommentRequestDto;
import org.handler.dto.request.ProcessingActionRequestDto;
import org.handler.dto.response.CommentResponseDto;
import org.handler.exception.CaseNotFoundException;
import org.handler.exception.CommentNotFoundException;
import org.handler.mapper.CommentMapper;
import org.handler.model.Case;
import org.handler.model.Comment;
import org.handler.model.User;
import org.handler.model.UserPrincipal;
import org.handler.model.enums.RuleType;
import org.handler.model.enums.UserType;
import org.handler.repository.CaseRepository;
import org.handler.repository.CommentRepository;
import org.handler.rule.RuleContext;
import org.handler.rule.RuleEngine;
import org.handler.service.ProcessingActionService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceImplTest {

    @Mock
    private CommentRepository commentRepository;
    @Mock
    private CommentMapper commentMapper;
    @Mock
    private CaseRepository caseRepository;
    @Mock
    private ProcessingActionService processingActionService;
    @Mock
    private RuleEngine ruleEngine;

    @InjectMocks
    private CommentServiceImpl commentService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void addComment_ShouldSaveCommentCreateProcessingActionAndApplyRules_WhenCurrentUserIsCaseOwner() {
        User owner = createUser(11L, UserType.CLIENT);
        setCurrentUser(owner);

        Case caseEntity = new Case();
        caseEntity.setId(100L);
        caseEntity.setUser(owner);

        CommentRequestDto request = CommentRequestDto.builder()
                .caseId(100L)
                .content("Please check this issue")
                .build();

        Comment savedComment = Comment.builder()
                .id(501L)
                .caseId(100L)
                .userId(11L)
                .content("Please check this issue")
                .createdAt(LocalDateTime.of(2026, 5, 1, 12, 0))
                .deleted(false)
                .build();

        CommentResponseDto responseDto = CommentResponseDto.builder()
                .id(501L)
                .caseId(100L)
                .userId(11L)
                .content("Please check this issue")
                .createdAt(LocalDateTime.of(2026, 5, 1, 12, 0))
                .deleted(false)
                .build();

        when(caseRepository.findById(100L)).thenReturn(Optional.of(caseEntity));
        doAnswer(invocation -> {
            CommentRequestDto dto = invocation.getArgument(0);
            Comment comment = invocation.getArgument(1);
            comment.setContent(dto.getContent());
            return null;
        }).when(commentMapper).toComment(eq(request), any(Comment.class));
        when(commentRepository.save(any(Comment.class))).thenReturn(savedComment);
        when(commentMapper.toCommentResponseDto(savedComment)).thenReturn(responseDto);

        commentService.addComment(request);

        ArgumentCaptor<Comment> commentCaptor = ArgumentCaptor.forClass(Comment.class);
        verify(commentRepository).save(commentCaptor.capture());
        Comment persisted = commentCaptor.getValue();
        assertEquals(11L, persisted.getUserId());
        assertEquals(100L, persisted.getCaseId());
        assertEquals("Please check this issue", persisted.getContent());

        ArgumentCaptor<ProcessingActionRequestDto> actionCaptor = ArgumentCaptor.forClass(ProcessingActionRequestDto.class);
        verify(processingActionService).createProcessingAction(actionCaptor.capture());
        ProcessingActionRequestDto actionRequest = actionCaptor.getValue();
        assertEquals("IN_PROGRESS", actionRequest.getStatus());
        assertEquals(100L, actionRequest.getCaseId());
        assertEquals("ADD_COMMENT", actionRequest.getParameters().get("actionType"));

        ArgumentCaptor<RuleContext> ruleContextCaptor = ArgumentCaptor.forClass(RuleContext.class);
        verify(ruleEngine).process(ruleContextCaptor.capture());
        RuleContext context = ruleContextCaptor.getValue();
        assertEquals(RuleType.REQUEST_CASE_COMMENT, context.getRuleType());
        assertEquals(100L, context.getData().get("caseId"));
    }

    @Test
    void addComment_ShouldAllowAdminEvenWhenNotOwner() {
        User admin = createUser(1L, UserType.ADMIN);
        setCurrentUser(admin);

        User caseOwner = createUser(2L, UserType.CLIENT);
        Case caseEntity = new Case();
        caseEntity.setId(200L);
        caseEntity.setUser(caseOwner);

        CommentRequestDto request = CommentRequestDto.builder()
                .caseId(200L)
                .content("Admin note")
                .build();

        Comment savedComment = Comment.builder()
                .id(502L)
                .caseId(200L)
                .userId(1L)
                .content("Admin note")
                .createdAt(LocalDateTime.now())
                .build();

        CommentResponseDto responseDto = CommentResponseDto.builder()
                .id(502L)
                .caseId(200L)
                .userId(1L)
                .createdAt(LocalDateTime.now())
                .content("Admin note")
                .build();

        when(caseRepository.findById(200L)).thenReturn(Optional.of(caseEntity));
        when(commentRepository.save(any(Comment.class))).thenReturn(savedComment);
        when(commentMapper.toCommentResponseDto(savedComment)).thenReturn(responseDto);

        commentService.addComment(request);

        verify(commentRepository).save(any(Comment.class));
        verify(processingActionService).createProcessingAction(any(ProcessingActionRequestDto.class));
        verify(ruleEngine).process(any(RuleContext.class));
    }

    @Test
    void addComment_ShouldThrowCaseNotFoundException_WhenCaseDoesNotExist() {
        setCurrentUser(createUser(99L, UserType.CLIENT));
        CommentRequestDto request = CommentRequestDto.builder().caseId(404L).content("Any content").build();

        when(caseRepository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(CaseNotFoundException.class, () -> commentService.addComment(request));

        verify(commentRepository, never()).save(any());
        verify(processingActionService, never()).createProcessingAction(any());
        verify(ruleEngine, never()).process(any());
    }

    @Test
    void addComment_ShouldThrowAccessDeniedException_WhenUserIsNotAuthenticated() {
        SecurityContextHolder.clearContext();

        User caseOwner = createUser(3L, UserType.CLIENT);
        Case caseEntity = new Case();
        caseEntity.setId(300L);
        caseEntity.setUser(caseOwner);

        CommentRequestDto request = CommentRequestDto.builder().caseId(300L).content("Unauthorized").build();
        when(caseRepository.findById(300L)).thenReturn(Optional.of(caseEntity));

        assertThrows(AccessDeniedException.class, () -> commentService.addComment(request));

        verify(commentRepository, never()).save(any());
    }

    @Test
    void addComment_ShouldThrowAccessDeniedException_WhenUserIsNotOwnerAndNotAdmin() {
        User currentUser = createUser(4L, UserType.CLIENT);
        setCurrentUser(currentUser);

        User caseOwner = createUser(5L, UserType.CLIENT);
        Case caseEntity = new Case();
        caseEntity.setId(301L);
        caseEntity.setUser(caseOwner);

        CommentRequestDto request = CommentRequestDto.builder().caseId(301L).content("Not allowed").build();
        when(caseRepository.findById(301L)).thenReturn(Optional.of(caseEntity));

        assertThrows(AccessDeniedException.class, () -> commentService.addComment(request));

        verify(commentRepository, never()).save(any());
        verify(processingActionService, never()).createProcessingAction(any());
    }

    @Test
    void addComment_ShouldThrowNullPointerException_WhenRequestIsNull() {
        assertThrows(NullPointerException.class, () -> commentService.addComment(null));

        verifyNoInteractions(caseRepository, commentRepository, processingActionService, ruleEngine);
    }

    @Test
    void updateComment_ShouldUpdateSaveAndCreateProcessingAction_WhenCommentExists() {
        setCurrentUser(createUser(8L, UserType.CLIENT));
        Long commentId = 700L;
        Comment existing = Comment.builder().id(commentId).caseId(77L).userId(8L).content("Old").deleted(false).build();
        CommentRequestDto request = CommentRequestDto.builder().caseId(77L).content("Updated content").build();
        Comment saved = Comment.builder().id(commentId).caseId(77L).userId(8L).content("Updated content").createdAt(LocalDateTime.now()).build();
        CommentResponseDto responseDto = CommentResponseDto.builder().id(commentId).caseId(77L).userId(8L).createdAt(LocalDateTime.now()).build();

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(existing));
        doAnswer(invocation -> {
            CommentRequestDto dto = invocation.getArgument(0);
            Comment target = invocation.getArgument(1);
            target.setContent(dto.getContent());
            return null;
        }).when(commentMapper).toComment(eq(request), eq(existing));
        when(commentRepository.save(existing)).thenReturn(saved);
        when(commentMapper.toCommentResponseDto(saved)).thenReturn(responseDto);

        commentService.updateComment(commentId, request);

        assertEquals("Updated content", existing.getContent());
        verify(commentRepository).save(existing);
        verify(processingActionService).createProcessingAction(any(ProcessingActionRequestDto.class));
    }

    @Test
    void updateComment_ShouldThrowCommentNotFoundException_WhenCommentMissing() {
        when(commentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(CommentNotFoundException.class, () -> commentService.updateComment(999L, CommentRequestDto.builder().build()));

        verify(commentRepository, never()).save(any());
        verify(processingActionService, never()).createProcessingAction(any());
    }

    @Test
    void deleteComment_ShouldMarkDeletedAndCreateProcessingAction_WhenCommentExists() {
        setCurrentUser(createUser(1L, UserType.CLIENT));
        Long commentId = 900L;
        Comment existing = Comment.builder().id(commentId).caseId(10L).userId(1L).content("To delete").deleted(false).build();
        CommentResponseDto responseDto = CommentResponseDto.builder()
                .id(commentId)
                .caseId(10L)
                .userId(1L)
                .createdAt(LocalDateTime.now())
                .content("To delete")
                .deleted(true)
                .build();

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(existing));
        when(commentMapper.toCommentResponseDto(existing)).thenReturn(responseDto);

        commentService.deleteComment(commentId);

        assertTrue(existing.isDeleted());
        verify(commentMapper).toCommentResponseDto(existing);
        verify(processingActionService).createProcessingAction(any(ProcessingActionRequestDto.class));
        verify(commentRepository, never()).save(any());
    }

    @Test
    void deleteComment_ShouldThrowCommentNotFoundException_WhenCommentMissing() {
        when(commentRepository.findById(901L)).thenReturn(Optional.empty());

        assertThrows(CommentNotFoundException.class, () -> commentService.deleteComment(901L));

        verify(commentMapper, never()).toCommentResponseDto(any());
        verify(processingActionService, never()).createProcessingAction(any());
    }

    @Test
    void updateComment_ShouldThrowAccessDeniedException_WhenCurrentUserIsNotCommentOwner() {
        setCurrentUser(createUser(100L, UserType.CLIENT));
        Comment existing = Comment.builder().id(55L).userId(200L).build();
        when(commentRepository.findById(55L)).thenReturn(Optional.of(existing));

        assertThrows(AccessDeniedException.class, () ->
                commentService.updateComment(55L, CommentRequestDto.builder().content("x").build()));

        verify(commentRepository, never()).save(any());
        verify(processingActionService, never()).createProcessingAction(any());
    }


    @Test
    void deleteComment_ShouldThrowAccessDeniedException_WhenCurrentUserIsNotCommentOwner() {
        setCurrentUser(createUser(100L, UserType.CLIENT));
        Comment existing = Comment.builder().id(57L).userId(200L).deleted(false).build();
        when(commentRepository.findById(57L)).thenReturn(Optional.of(existing));

        assertThrows(AccessDeniedException.class, () -> commentService.deleteComment(57L));

        assertFalse(existing.isDeleted());
        verify(processingActionService, never()).createProcessingAction(any());
    }


    @Test
    void getCommentsByCaseId_ShouldThrowCaseNotFoundException_WhenCaseDoesNotExist() {
        when(caseRepository.existsById(222L)).thenReturn(false);

        assertThrows(CaseNotFoundException.class, () -> commentService.getCommentsByCaseId(222L));

        verify(commentRepository, never()).findByCaseId(anyLong());
    }

    @Test
    void addAiCommentResponse_ShouldSaveAndCreateProcessingAction() {
        CommentRequestDto request = CommentRequestDto.builder()
                .caseId(333L)
                .userId(44L)
                .content("AI generated text")
                .build();

        Comment saved = Comment.builder()
                .id(808L)
                .caseId(333L)
                .userId(44L)
                .content("AI generated text")
                .createdAt(LocalDateTime.of(2026, 5, 1, 13, 0))
                .build();

        CommentResponseDto responseDto = CommentResponseDto.builder()
                .id(808L)
                .caseId(333L)
                .userId(44L)
                .content("AI generated text")
                .createdAt(LocalDateTime.of(2026, 5, 1, 13, 0))
                .build();

        when(commentRepository.save(any(Comment.class))).thenReturn(saved);
        when(commentMapper.toCommentResponseDto(saved)).thenReturn(responseDto);

        commentService.addAiCommentResponse(request);

        verify(commentMapper).toComment(eq(request), any(Comment.class));
        verify(commentRepository).save(any(Comment.class));

        ArgumentCaptor<ProcessingActionRequestDto> actionCaptor = ArgumentCaptor.forClass(ProcessingActionRequestDto.class);
        verify(processingActionService).createProcessingAction(actionCaptor.capture());
        assertEquals("AI_COMMENT", actionCaptor.getValue().getParameters().get("actionType"));
        assertEquals(333L, actionCaptor.getValue().getCaseId());
    }


    private User createUser(Long id, UserType type) {
        return User.builder()
                .id(id)
                .username("user" + id)
                .password("secret")
                .email("user" + id + "@test.com")
                .type(type)
                .build();
    }

    private void setCurrentUser(User user) {
        UserPrincipal principal = new UserPrincipal(user);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
