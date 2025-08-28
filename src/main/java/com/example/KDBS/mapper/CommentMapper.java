package com.example.KDBS.mapper;

import com.example.KDBS.dto.request.CommentRequest;
import com.example.KDBS.dto.response.CommentResponse;
import com.example.KDBS.model.ForumComment;
import org.mapstruct.*;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface CommentMapper {

    @Mapping(target = "forumCommentId", ignore = true)
    @Mapping(target = "imgPath", ignore = true) // Set manually in service after file upload
    @Mapping(target = "react", constant = "0") // Initial react count
    @Mapping(target = "user", ignore = true) // Set manually in service
    @Mapping(target = "forumPost", ignore = true) // Set manually in service
    @Mapping(target = "createdAt", ignore = true) // Handled by @PrePersist
    ForumComment toEntity(CommentRequest dto);

    CommentResponse toResponse(ForumComment entity);
}