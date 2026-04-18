package org.handler.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CaseRequestDto {
    @NotBlank(message = "Case type is required")
    private String type;

    @NotBlank(message = "Case subtype is required")
    private String subtype;

    private Long userId;

    @NotBlank(message = "Title is required")
    @Size(min = 5, max = 50, message = "Title must be between 5 and 50 characters")
    private String title;

    @NotEmpty(message = "Processing parameters are required")
    private Map<String, String> parameters;

    private String status;
}
