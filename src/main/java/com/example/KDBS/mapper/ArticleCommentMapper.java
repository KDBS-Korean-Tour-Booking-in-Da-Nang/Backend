package com.example.KDBS.mapper;

import com.example.KDBS.dto.response.ArticleCommentResponse;
import com.example.KDBS.model.ArticleComment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ArticleCommentMapper {

    @Mapping(target = "articleCommentId", source = "articleCommentId")
    @Mapping(target = "content", source = "content")
    @Mapping(target = "imgPath", source = "imgPath")
    @Mapping(target = "react", source = "react")
    @Mapping(target = "createdAt", source = "createdAt")

    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "userAvatar", source = "user.avatar")
    @Mapping(target = "userEmail", source = "user.email")

    @Mapping(target = "articleId", source = "article.articleId")
    @Mapping(target = "parentCommentId", source = "parentComment.articleCommentId")
    ArticleCommentResponse toResponse(ArticleComment comment);
}
