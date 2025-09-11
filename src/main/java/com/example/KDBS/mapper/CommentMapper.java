package com.example.KDBS.mapper;

import com.example.KDBS.dto.response.CommentResponse;
import com.example.KDBS.model.ForumComment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CommentMapper {

    @Mapping(target = "forumCommentId", source = "forumCommentId")
    @Mapping(target = "content", source = "content")
    @Mapping(target = "imgPath", source = "imgPath")
    @Mapping(target = "react", source = "react")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "userAvatar", source = "user.avatar")
    @Mapping(target = "userEmail", source = "user.email")
    @Mapping(target = "forumPostId", source = "forumPost.forumPostId")
    @Mapping(target = "parentCommentId", source = "parentComment.forumCommentId")
    CommentResponse toResponse(ForumComment comment);
}