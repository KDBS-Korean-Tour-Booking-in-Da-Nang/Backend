package com.example.KDBS.service;

import com.example.KDBS.dto.request.UserRegisterRequest;
import com.example.KDBS.dto.response.UserResponse;
import com.example.KDBS.entity.User;
import com.example.KDBS.enums.Role;
import com.example.KDBS.exception.AppException;
import com.example.KDBS.exception.ErrorCode;
import com.example.KDBS.mapper.UserMapper;
import com.example.KDBS.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin("*")
public class UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private PasswordEncoder passwordEncoder;


    public String createUser(UserRegisterRequest request) throws IOException {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.EMAIL_EXISTED);
        }

//        String otp = otpUtil.generateOtp();
//        try {
//            emailUtil.sendOtpEmail(request.getEmail(), otp);
//        } catch (MessagingException e) {
//            throw new RuntimeException("Unable to send otp please try again");
//        }

        User user = userMapper.toUser(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.USER.name());
        user.setStatus("noStatus");
//        user.setOtp(otp);
//        user.setGenerateOtpTime(LocalDateTime.now());

        userRepository.save(user);

//        tuy theo viec xu ly nhu nao
//        return "To verify this is your email account, we will send a confirmation code to this email. Please check your email to receive the verification code to activate your account";

        return "Succed";
    }

    public List<UserResponse> getAllUsers(){
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(userMapper::toUserResponse)
                .toList();
    }
}
