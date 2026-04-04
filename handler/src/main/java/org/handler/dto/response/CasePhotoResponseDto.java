package org.handler.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CasePhotoResponseDto {
    private Long id;
    private String fileName;
    private String filePath;
}
