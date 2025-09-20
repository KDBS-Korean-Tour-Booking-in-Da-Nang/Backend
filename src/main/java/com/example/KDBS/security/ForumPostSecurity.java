package com.example.KDBS.security;

import com.example.KDBS.exception.AppException;
import com.example.KDBS.exception.ErrorCode;
import com.example.KDBS.model.ForumPost;
import com.example.KDBS.model.User;
import com.example.KDBS.repository.ForumPostRepository;
import com.example.KDBS.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("forumPostSecurity")
public class ForumPostSecurity {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ForumPostRepository forumPostRepository;

    public boolean canDeletePost(Long postId, String userEmail) {
        // Lấy user request
        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // Lấy bài post
        ForumPost post = forumPostRepository.findById(postId)
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));

        // Lấy role hiện tại
        String role = currentUser.getRole().name(); // admin, staff, company, user

        // Owner của bài
        User owner = post.getUser();
        String ownerRole = owner.getRole().name();

        // Quy tắc phân quyền
        if (role.equalsIgnoreCase("ADMIN")) {
            return true; // admin xóa được hết
        }

        if (role.equalsIgnoreCase("STAFF")) {
            if (ownerRole.equalsIgnoreCase("ADMIN")) {
                throw new AppException(ErrorCode.STAFF_CANNOT_DELETE_ADMIN_POSTS);
            }
            return true; // staff xóa bài của mọi người trừ admin
        }

        if (role.equalsIgnoreCase("COMPANY") || role.equalsIgnoreCase("USER")) {
            if (!owner.getEmail().equals(userEmail)) {
                throw new AppException(ErrorCode.USER_OR_COMPANY_CAN_ONLY_DELETE_THEIR_OWN_POSTS);
            }
            return true;
        }

        throw new AppException(ErrorCode.YOU_DO_NOT_HAVE_PERMISSION_TO_DELETE_THIS_POST);
    }
}
