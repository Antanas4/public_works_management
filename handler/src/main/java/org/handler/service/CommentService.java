package org.handler.service;

import org.handler.dto.request.CommentRequestDto;
import org.handler.dto.response.CommentResponseDto;

import java.util.List;

public interface CommentService {
    void addComment(CommentRequestDto commentRequestDto);

    void updateComment(Long commentId, CommentRequestDto commentRequestDto);

    void deleteComment(Long commentId);

    List<CommentResponseDto> getCommentsByCaseId(Long caseId);

    void addAiCommentResponse(CommentRequestDto commentRequestDto);
}
