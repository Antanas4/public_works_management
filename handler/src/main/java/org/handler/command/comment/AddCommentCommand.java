package org.handler.command.comment;

import lombok.RequiredArgsConstructor;
import org.handler.dto.request.CommentRequestDto;
import org.handler.service.CommentService;

@RequiredArgsConstructor
public class AddCommentCommand implements CommentCommand {
    private final CommentRequestDto commentRequestDto;
    private final CommentService commentService;

    @Override
    public void execute() {
        commentService.addComment(commentRequestDto);
    }
}
