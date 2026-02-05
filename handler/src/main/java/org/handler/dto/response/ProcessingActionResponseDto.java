package org.handler.dto.response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProcessingActionResponseDto {
    private Long id;
    private LocalDateTime createdAt;
    private String status;
    private Map<String, String> parameters;
    private Long caseId;
}
