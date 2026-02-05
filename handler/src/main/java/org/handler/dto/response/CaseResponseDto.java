package org.handler.dto.response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CaseResponseDto {
    private Long id;
    private String type;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
    private Long userId;
    private String status;
    private String title;
    private List<ProcessingActionResponseDto> processingActions;
}
