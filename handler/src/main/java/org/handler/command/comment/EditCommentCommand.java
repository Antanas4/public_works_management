package org.handler.command.comment;

import lombok.RequiredArgsConstructor;
import org.handler.dto.request.CommentRequestDto;
import org.handler.service.CommentService;

@RequiredArgsConstructor
public class EditCommentCommand implements CommentCommand{
    private final CommentRequestDto commentRequestDto;
    private final CommentService commentService;
    private final Long commentId;


    @Override
    public void execute() {
        commentService.updateComment(commentId, commentRequestDto);
    }
}
