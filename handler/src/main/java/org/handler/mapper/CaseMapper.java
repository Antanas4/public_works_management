package org.handler.mapper;

import org.handler.dto.request.CaseRequestDto;
import org.handler.dto.response.CaseResponseDto;
import org.handler.model.Case;
import org.handler.model.ProcessingAction;
import org.handler.model.User;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CaseMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "modifiedAt", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "processingActions", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void toCase(CaseRequestDto dto,
                @MappingTarget Case caseEntity,
                @Context User user,
                @Context List<ProcessingAction> processingActions);


    @Mapping(source = "user.id", target = "userId")
    CaseResponseDto toCaseResponseDto(Case caseRef);

    @AfterMapping
    default void enrichWithContext(@MappingTarget Case caseEntity,
                                   @Context User user,
                                   @Context List<ProcessingAction> processingActions) {
        caseEntity.setUser(user);
        caseEntity.setProcessingActions(processingActions);
    }
}
