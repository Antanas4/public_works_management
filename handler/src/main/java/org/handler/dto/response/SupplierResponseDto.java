package org.handler.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.handler.model.enums.CaseSubtype;

import java.util.Map;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class SupplierResponseDto {
    private Long id;
    private String name;
    private String confidence;
    private String reason;
    private String source;
    private Set<CaseSubtype> handledCaseSubtypes;
    private Map<String, String> metadata;
}