package com.example.KDBS.security;

import com.example.KDBS.exception.AppException;
import com.example.KDBS.exception.ErrorCode;
import com.example.KDBS.model.ForumComment;
import com.example.KDBS.model.User;
import com.example.KDBS.repository.ForumCommentRepository;
import com.example.KDBS.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("forumCommentSecurity")
public class ForumCommentSecurity {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ForumCommentRepository forumCommentRepository;

    public boolean canDeleteComment(Long commentId, String userEmail) {
        // Lấy user request
        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // Lấy comment
        ForumComment comment = forumCommentRepository.findById(commentId)
                .orElseThrow(() -> new AppException(ErrorCode.COMMENT_NOT_FOUND));

        // Lấy role hiện tại
        String role = currentUser.getRole().name(); // ADMIN, STAFF, COMPANY, USER

        // Owner của comment
        User owner = comment.getUser();
        String ownerRole = owner.getRole().name();

        // Quy tắc phân quyền
        if (role.equalsIgnoreCase("ADMIN")) {
            return true; // admin xóa được hết
        }

        if (role.equalsIgnoreCase("STAFF")) {
            if (ownerRole.equalsIgnoreCase("ADMIN")) {
                throw new AppException(ErrorCode.STAFF_CANNOT_DELETE_ADMIN_COMMENTS);
            }
            return true; // staff xóa comment của mọi người trừ admin
        }

        if (role.equalsIgnoreCase("COMPANY") || role.equalsIgnoreCase("USER")) {
            if (!owner.getEmail().equals(userEmail)) {
                throw new AppException(ErrorCode.USER_OR_COMPANY_CAN_ONLY_DELETE_THEIR_OWN_COMMENTS);
            }
            return true;
        }

        throw new AppException(ErrorCode.YOU_DO_NOT_HAVE_PERMISSION_TO_DELETE_THIS_COMMENT);
    }

}
