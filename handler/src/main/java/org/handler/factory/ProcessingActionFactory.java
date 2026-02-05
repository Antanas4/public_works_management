package org.handler.factory;

import lombok.extern.slf4j.Slf4j;
import org.handler.dto.request.ProcessingActionRequestDto;
import org.handler.dto.response.CommentResponseDto;
import org.handler.model.enums.CommentAction;
import org.handler.model.enums.ProcessingStatus;

import java.util.Map;

@Slf4j
public class ProcessingActionFactory {
    private static final String ACTION_TYPE = "actionType";
    private static final String USER_ID = "userId";
    private static final String COMMENT_ID = "commentId";
    private static final String CREATED_AT = "createdAt";

    public static ProcessingActionRequestDto createRequestFromCommentAction(CommentAction commentAction,
                                                                            ProcessingStatus processingStatus,
                                                                            CommentResponseDto commentResponseDto) {
        Map<String, String> parameters = buildParameters(commentAction, commentResponseDto);

        return ProcessingActionRequestDto.builder()
                .caseId(commentResponseDto.getCaseId())
                .status(String.valueOf(processingStatus))
                .parameters(parameters)
                .build();
    }

    private static Map<String, String> buildParameters(CommentAction commentAction, CommentResponseDto commentResponseDto) {
        return Map.of(
                ACTION_TYPE, commentAction.name(),
                USER_ID, String.valueOf(commentResponseDto.getUserId()),
                COMMENT_ID, String.valueOf(commentResponseDto.getId()),
                CREATED_AT, String.valueOf(commentResponseDto.getCreatedAt())
        );
    }
}
