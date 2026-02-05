package org.handler.service.impl;

import lombok.RequiredArgsConstructor;
import org.handler.dto.request.CommentRequestDto;
import org.handler.dto.request.ProcessingActionRequestDto;
import org.handler.dto.response.CommentResponseDto;
import org.handler.exception.CaseNotFoundException;
import org.handler.exception.CommentNotFoundException;
import org.handler.mapper.CommentMapper;
import org.handler.model.Comment;
import org.handler.model.enums.CommentAction;
import org.handler.model.enums.ProcessingStatus;
import org.handler.model.enums.RuleType;
import org.handler.repository.CaseRepository;
import org.handler.repository.CommentRepository;
import org.handler.rule.RuleContext;
import org.handler.rule.RuleEngine;
import org.handler.service.CommentService;
import org.handler.service.ProcessingActionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static org.handler.factory.ProcessingActionFactory.createRequestFromCommentAction;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;
    private final CaseRepository caseRepository;
    private final ProcessingActionService processingActionService;
    private final RuleEngine ruleEngine;

    @Override
    @Transactional
    public void addComment(CommentRequestDto commentRequestDto) {
        boolean caseExists = caseRepository.existsById(commentRequestDto.getCaseId());
        if (!caseExists) {
            throw new CaseNotFoundException("Case not found with ID: " + commentRequestDto.getCaseId());
        }

        commentRequestDto.setUserId(9L); //delete after logic of getting user id is added!!

        Comment comment = new Comment();
        commentMapper.toComment(commentRequestDto, comment);

        CommentResponseDto commentResponseDto = commentMapper.toCommentResponseDto(commentRepository.save(comment));

        createCommentProcessingAction(commentResponseDto, CommentAction.ADD_COMMENT);

        applyCommentRules(commentRequestDto);
    }

    @Override
    @Transactional
    public void updateComment(Long commentId, CommentRequestDto commentRequestDto) {
        Comment comment = findCommentById(commentId);

        commentMapper.toComment(commentRequestDto, comment);

        CommentResponseDto commentResponseDto = commentMapper.toCommentResponseDto(commentRepository.save(comment));

        createCommentProcessingAction(commentResponseDto, CommentAction.EDIT_COMMENT);
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId) {
        Comment comment = findCommentById(commentId);
        comment.setDeleted(true);

        CommentResponseDto commentResponseDto = commentMapper.toCommentResponseDto(comment);

        createCommentProcessingAction(commentResponseDto, CommentAction.DELETE_COMMENT);
    }

    @Override
    public List<CommentResponseDto> getCommentsByCaseId(Long caseId) {
        boolean caseExists = caseRepository.existsById(caseId);
        if (!caseExists) {
            throw new CaseNotFoundException("Case not found with ID: " + caseId);
        }

        return commentRepository.findByCaseId(caseId)
                .stream()
                .map(comment -> {
                    if (comment.isDeleted()) {
                        CommentResponseDto deletedDto = new CommentResponseDto();
                        deletedDto.setDeleted(true);
                        deletedDto.setContent("Comment deleted");
                        return deletedDto;
                    } else {
                        return commentMapper.toCommentResponseDto(comment);
                    }
                })
                .toList();
    }

    @Override
    public void addAiCommentResponse(CommentRequestDto commentRequestDto) {
        Comment comment = new Comment();
        commentMapper.toComment(commentRequestDto, comment);

        CommentResponseDto commentResponseDto = commentMapper.toCommentResponseDto(commentRepository.save(comment));

        createCommentProcessingAction(commentResponseDto, CommentAction.AI_COMMENT);
    }

    private Comment findCommentById(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException("Comment not found with id: " + commentId));
    }

    private void createCommentProcessingAction(CommentResponseDto commentResponseDto, CommentAction commentAction) {
        ProcessingActionRequestDto processingActionRequestDto = createRequestFromCommentAction(
                commentAction,
                ProcessingStatus.IN_PROGRESS,
                commentResponseDto);

        processingActionService.createProcessingAction(processingActionRequestDto);
    }

    private void applyCommentRules(CommentRequestDto commentRequestDto) {
        ruleEngine.process(new RuleContext(
                RuleType.REQUEST_CASE_COMMENT,
                Map.of("caseId", commentRequestDto.getCaseId())
        ));
    }
}
