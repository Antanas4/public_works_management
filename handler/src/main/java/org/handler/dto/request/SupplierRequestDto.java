package org.handler.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.Set;

@Getter
@Setter
public class SupplierRequestDto {
    private Long id;
    @NotBlank(message = "Supplier name is required")
    private String name;
    @NotBlank(message = "Supplier source is required")
    private String source;
    private Set<String> handledCaseSubtypes;
    private Map<String, String> metadata;
}
