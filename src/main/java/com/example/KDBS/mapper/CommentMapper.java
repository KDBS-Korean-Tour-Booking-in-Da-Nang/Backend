package com.example.KDBS.mapper;

import com.example.KDBS.dto.response.CommentResponse;
import com.example.KDBS.model.ForumComment;
import org.springframework.stereotype.Component;

@Component
public class CommentMapper {

    public CommentResponse toResponse(ForumComment comment) {
        if (comment == null) {
            return null;
        }

        return CommentResponse.builder()
                .forumCommentId(comment.getForumCommentId())
                .content(comment.getContent())
                .imgPath(comment.getImgPath())
                .react(comment.getReact())
                .createdAt(comment.getCreatedAt())
                .username(comment.getUser() != null ? comment.getUser().getUsername() : null)
                .userAvatar(comment.getUser() != null ? comment.getUser().getAvatar() : null)
                .forumPostId(comment.getForumPost() != null ? comment.getForumPost().getForumPostId() : null)
                .parentCommentId(
                        comment.getParentComment() != null ? comment.getParentComment().getForumCommentId() : null)
                .build();
    }
}