package com.example.KDBS.service;

import com.example.KDBS.dto.request.StaffCreateRequest;
import com.example.KDBS.dto.request.UsernameAuthenticationRequest;
import com.example.KDBS.dto.response.UserResponse;
import com.example.KDBS.enums.Role;
import com.example.KDBS.enums.StaffTask;
import com.example.KDBS.enums.Status;
import com.example.KDBS.exception.AppException;
import com.example.KDBS.exception.ErrorCode;
import com.example.KDBS.mapper.UserMapper;
import com.example.KDBS.model.User;
import com.example.KDBS.repository.UserRepository;
import com.example.KDBS.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class StaffService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserResponse createStaffAccount(StaffCreateRequest request) {
        // Check trùng username
        userRepository.findByUsername(request.getUsername())
                .ifPresent(u -> { throw new AppException(ErrorCode.USERNAME_EXISTED); });

        User staff = userMapper.toStaff(request);

        staff.setRole(Role.STAFF);
        staff.setStatus(Status.UNBANNED);
        staff.setCreatedAt(LocalDateTime.now());
        staff.setPassword(passwordEncoder.encode(request.getPassword()));

        userRepository.save(staff);
        return userMapper.toUserResponse(staff);
    }

    @Transactional
    public UserResponse setUserBanStatus(int userId, boolean ban) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (ban) {
            user.setStatus(Status.BANNED);
        } else {
            user.setStatus(Status.UNBANNED);
        }

        return userMapper.toUserResponse(user);
    }

    @Transactional
    public UserResponse updateUserRole(int userId, Role newRole) {
        // Lấy username từ token
        String username = SecurityUtils.getCurrentUsername();

        // Tìm user hiện tại
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.STAFF_NOT_FOUND));

        // Nếu là STAFF → cần check staffTask
        if (user.getRole() == Role.STAFF) {
            if (user.getStaffTask() != StaffTask.COMPANY_REQUEST_AND_APPROVE_ARTICLE) {
                throw new AppException(ErrorCode.THIS_STAFF_ACCOUNT_IS_NOT_AUTHORIZED_FOR_THIS_ACTION);
            }
        }

        // Check user target
        User target = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        target.setRole(newRole);
        userRepository.save(target);

        return userMapper.toUserResponse(target);
    }


}
