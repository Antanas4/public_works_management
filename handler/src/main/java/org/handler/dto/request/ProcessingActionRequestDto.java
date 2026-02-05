package org.handler.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProcessingActionRequestDto {
    private String status;

    private Long caseId;

    @NotEmpty(message = "Processing parameters are required")
    private Map<String, String> parameters;
}
