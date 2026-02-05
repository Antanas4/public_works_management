package org.handler.command.comment;

import lombok.RequiredArgsConstructor;
import org.handler.service.CommentService;

@RequiredArgsConstructor
public class DeleteCommentCommand implements CommentCommand{
    private final CommentService commentService;
    private final Long commentId;

    @Override
    public void execute() {
        commentService.deleteComment(commentId);
    }
}
