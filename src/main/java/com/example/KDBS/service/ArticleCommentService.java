package com.example.KDBS.service;

import com.example.KDBS.dto.request.ArticleCommentRequest;
import com.example.KDBS.dto.response.ArticleCommentResponse;
import com.example.KDBS.enums.Role;
import com.example.KDBS.exception.AppException;
import com.example.KDBS.exception.ErrorCode;
import com.example.KDBS.mapper.ArticleCommentMapper;
import com.example.KDBS.model.Article;
import com.example.KDBS.model.ArticleComment;
import com.example.KDBS.model.User;
import com.example.KDBS.repository.ArticleCommentRepository;
import com.example.KDBS.repository.ArticleRepository;
import com.example.KDBS.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ArticleCommentService {

    private final ArticleCommentRepository commentRepository;
    private final UserRepository userRepository;
    private final ArticleRepository articleRepository;
    private final ArticleCommentMapper mapper;

    @Transactional
    public ArticleCommentResponse createComment(ArticleCommentRequest req) {

        User user = userRepository.findByEmail(req.getUserEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Article article = articleRepository.findById(req.getArticleId())
                .orElseThrow(() -> new AppException(ErrorCode.ARTICLE_NOT_FOUND));

        ArticleComment comment = ArticleComment.builder()
                .content(req.getContent())
                .imgPath(req.getImgPath())
                .react(0)
                .user(user)
                .article(article)
                .build();

        // reply
        if (req.getParentCommentId() != null) {
            ArticleComment parent = commentRepository.findById(req.getParentCommentId())
                    .orElseThrow(() -> new AppException(ErrorCode.PARENT_COMMENT_NOT_FOUND));
            comment.setParentComment(parent);
        }

        comment = commentRepository.save(comment);
        return mapper.toResponse(comment);
    }

    @Transactional(readOnly = true)
    public List<ArticleCommentResponse> getCommentsByArticleId(Long articleId) {
        return commentRepository.findByArticle_ArticleId(articleId)
                .stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ArticleCommentResponse> getReplies(Long parentId) {
        return commentRepository
                .findByParentComment_ArticleCommentIdOrderByCreatedAtAsc(parentId)
                .stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteComment(Long commentId, String requesterEmail) {

        User requester = userRepository.findByEmail(requesterEmail)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        ArticleComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new AppException(ErrorCode.COMMENT_NOT_FOUND));

        User owner = comment.getUser();

        Role reqRole = requester.getRole();
        Role ownerRole = owner.getRole();

        // PERMISSION CHECK
        if (reqRole == Role.ADMIN) { }
        else if (reqRole == Role.STAFF && ownerRole == Role.ADMIN) {
            throw new AppException(ErrorCode.STAFF_CANNOT_DELETE_ADMIN_COMMENTS);
        }
        else if (!owner.getEmail().equals(requesterEmail)) {
            throw new AppException(ErrorCode.USER_OR_COMPANY_CAN_ONLY_DELETE_THEIR_OWN_COMMENTS);
        }

        commentRepository.delete(comment);
    }

    @Transactional
    public ArticleCommentResponse updateComment(Long commentId, ArticleCommentRequest req) {

        ArticleComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new AppException(ErrorCode.COMMENT_NOT_FOUND));

        User requester = userRepository.findByEmail(req.getUserEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // only owner can update
        if (!comment.getUser().getEmail().equals(req.getUserEmail())) {
            throw new AppException(ErrorCode.COMMENT_OWNER_CAN_ONLY_UPDATE_THEIR_OWN_COMMENTS);
        }

        if (req.getContent() != null) comment.setContent(req.getContent());
        if (req.getImgPath() != null) comment.setImgPath(req.getImgPath());

        ArticleComment saved = commentRepository.save(comment);
        return mapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public ArticleCommentResponse getCommentById(Long id) {
        ArticleComment comment = commentRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.COMMENT_NOT_FOUND));
        return mapper.toResponse(comment);
    }
}
