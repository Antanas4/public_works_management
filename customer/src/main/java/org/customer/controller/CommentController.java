package org.customer.controller;

import lombok.RequiredArgsConstructor;
import org.handler.command.comment.*;
import org.handler.dto.request.CommentRequestDto;
import org.handler.dto.response.CommentResponseDto;
import org.handler.service.CommentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {
    private final CommentInvoker commentInvoker;
    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<Map<String, String>> addComment(@RequestBody CommentRequestDto commentRequestDto) {
        CommentCommand commentCommand = new AddCommentCommand(commentRequestDto, commentService);
        commentInvoker.invoke(commentCommand);
        return ResponseEntity.ok(Map.of("message", "Comment added for sure."));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, String>> updateComment(@PathVariable Long id, @RequestBody CommentRequestDto commentRequestDto) {
        CommentCommand commentCommand = new EditCommentCommand(commentRequestDto, commentService, id);
        commentInvoker.invoke(commentCommand);
        return ResponseEntity.ok(Map.of("message", "Comment updated."));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteComment(@PathVariable Long id) {
        CommentCommand commentCommand = new DeleteCommentCommand(commentService, id);
        commentInvoker.invoke(commentCommand);
        return ResponseEntity.ok(Map.of("message", "Comment deleted."));
    }

    @GetMapping("/case/{caseId}")
    public ResponseEntity<List<CommentResponseDto>> getCommentsByCaseId(@PathVariable Long caseId) {
        return ResponseEntity.ok(commentService.getCommentsByCaseId(caseId));
    }
}
