package org.handler.mapper;

import org.handler.dto.request.CommentRequestDto;
import org.handler.dto.response.CommentResponseDto;
import org.handler.model.Comment;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CommentMapper {
    void toComment(CommentRequestDto commentRequestDto, @MappingTarget Comment comment);

    CommentResponseDto toCommentResponseDto(Comment comment);
}
