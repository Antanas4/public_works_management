package org.handler.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CommentResponseDto {
    private Long id;
    private Long userId;
    private Long caseId;
    private LocalDateTime createdAt;
    private String content;
    private boolean deleted;
}
