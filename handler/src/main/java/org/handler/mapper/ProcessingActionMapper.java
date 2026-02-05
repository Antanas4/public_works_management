package org.handler.mapper;

import org.handler.dto.request.ProcessingActionRequestDto;
import org.handler.dto.response.ProcessingActionResponseDto;
import org.handler.model.ProcessingAction;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ProcessingActionMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "modifiedAt", ignore = true)
    @Mapping(target = "caseRef", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void toProcessingAction(ProcessingActionRequestDto dto, @MappingTarget ProcessingAction processingAction);

    @Mapping(source = "caseRef.id", target = "caseId")
    ProcessingActionResponseDto toProcessingActionResponseDto(ProcessingAction processingAction);
}
