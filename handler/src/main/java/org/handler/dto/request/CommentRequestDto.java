package org.handler.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CommentRequestDto {
    private Long userId;

    @NotNull(message = "Case ID is required")
    private Long caseId;

    @NotBlank(message = "Content is required")
    @Size(min = 5, max = 255, message = "Content must be between 5 and 255 character")
    private String content;
}
