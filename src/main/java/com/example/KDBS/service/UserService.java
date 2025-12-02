package com.example.KDBS.service;

import com.example.KDBS.dto.request.BusinessLicenseRequest;
import com.example.KDBS.dto.request.IdCardApiRequest;
import com.example.KDBS.dto.request.UserRegisterRequest;
import com.example.KDBS.dto.request.UserUpdateRequest;
import com.example.KDBS.dto.response.BusinessUploadStatusResponse;
import com.example.KDBS.dto.response.UserResponse;
import com.example.KDBS.enums.OTPPurpose;
import com.example.KDBS.enums.Role;
import com.example.KDBS.enums.Status;
import com.example.KDBS.exception.AppException;
import com.example.KDBS.exception.ErrorCode;
import com.example.KDBS.mapper.UserIdCardMapper;
import com.example.KDBS.mapper.UserMapper;
import com.example.KDBS.model.BusinessLicense;
import com.example.KDBS.model.User;
import com.example.KDBS.model.UserIdCard;
import com.example.KDBS.repository.BusinessLicenseRepository;
import com.example.KDBS.repository.UserIdCardRepository;
import com.example.KDBS.repository.UserRepository;
import com.example.KDBS.utils.FileStorageService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final UserIdCardMapper userIdCardMapper;
    private final PasswordEncoder passwordEncoder;
    private final OTPService otpService;
    private final BusinessLicenseRepository businessLicenseRepository;
    private final UserIdCardRepository userIdCardRepository;
    private final FileStorageService fileStorageService;

    @Value("${fpt.ai.url}")
    private String API_URL;
    @Value("${fpt.ai.key}")
    private String API_KEY;

    public String createUser(UserRegisterRequest request) {
        LocalDateTime now = LocalDateTime.now();

        // 1. Check theo EMAIL trước
        User userByEmail = userRepository.findByEmail(request.getEmail()).orElse(null);

        if (userByEmail != null) {
            // Email đã tồn tại
            if (userByEmail.getStatus() != Status.UNVERIFIED) {
                // Đã verify rồi -> không cho đăng ký lại
                throw new AppException(ErrorCode.EMAIL_EXISTED);
            }

            // UNVERIFIED
            if (userByEmail.getCreatedAt().isAfter(now.minusDays(3))) {
                // Trong 3 ngày -> coi như user đăng ký lại -> UPDATE thông tin
                userByEmail.setPassword(passwordEncoder.encode(request.getPassword()));
                userByEmail.setRole(resolveRole(request.getRole()));
                // (username giữ nguyên vì thường form đăng ký lại sẽ dùng cùng username)

                userRepository.save(userByEmail);

                otpService.generateAndSendOTP(userByEmail.getEmail(), OTPPurpose.VERIFY_EMAIL);
                return "Email already registered but not verified. Info updated and OTP resent.";
            } else {
                // Quá 3 ngày -> xóa user cũ, cho phép tạo mới
                userRepository.delete(userByEmail);
            }
        }

        // 2. Check USERNAME
        User userByUsername = userRepository.findByUsername(request.getUsername()).orElse(null);

        if (userByUsername != null) {
            if (userByUsername.getStatus() == Status.UNVERIFIED) {
                // Username thuộc user chưa verify -> xóa để ghi đè
                userRepository.delete(userByUsername);
            } else {
                // Username thuộc user đã verify -> chặn
                throw new AppException(ErrorCode.USERNAME_EXISTED);
            }
        }

        // 3. Tạo user mới
        User newUser = userMapper.toUser(request);
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));
        newUser.setRole(resolveRole(request.getRole()));
        newUser.setStatus(Status.UNVERIFIED);
        newUser.setCreatedAt(now);

        userRepository.save(newUser);
        otpService.generateAndSendOTP(newUser.getEmail(), OTPPurpose.VERIFY_EMAIL);

        return "Registration successful. Please check your email for verification code.";
    }

    private Role resolveRole(String roleStr) {
        try {
            return Role.valueOf(roleStr.toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            return Role.USER;
        }
    }

    @Transactional
    public boolean verifyEmail(String email, String otpCode) {
        boolean isValid = otpService.verifyOTP(email, otpCode, OTPPurpose.VERIFY_EMAIL);

        if (!isValid) {
            return false;
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (user.getRole() == Role.COMPANY) {
            user.setStatus(Status.COMPANY_PENDING);
        } else {
            user.setStatus(Status.UNBANNED);
        }

        userRepository.save(user);
        return true;
    }

    public UserResponse getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        return userMapper.toUserResponse(user);
    }

        @Transactional
        public UserResponse updateUser(String email, UserUpdateRequest request, MultipartFile avatarImg) throws IOException {
            // Tìm user theo email
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

            if (request.getUsername() != null) {
                userRepository.findByUsername(request.getUsername())
                        .filter(u -> u.getUserId() != user.getUserId())
                        .ifPresent(u -> { throw new AppException(ErrorCode.USERNAME_EXISTED); });
            }

            if (request.getPhone() != null) {
                userRepository.findByPhone(request.getPhone())
                        .filter(u -> u.getUserId() != user.getUserId())
                        .ifPresent(u -> { throw new AppException(ErrorCode.PHONE_EXISTED); });
            }

            userMapper.updateUserFromDto(request, user);
            if (avatarImg != null && !avatarImg.isEmpty()) {
                String newAvatar = fileStorageService.uploadFile(avatarImg, "/users/avatar");
                user.setAvatar(newAvatar);
            }
            userRepository.save(user);
            return userMapper.toUserResponse(user);
        }

    @GetMapping
    public List<UserResponse> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(userMapper::toUserResponse)
                .toList();
    }

    @Transactional
    public void updateBusinessLicenseAndIdCard(BusinessLicenseRequest request) throws Exception {
        // Fetch user once
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.EMAIL_NOT_EXISTED));

        // ===== BUSINESS LICENSE =====
        if (user.getBusinessLicense() != null) {
            throw new AppException(ErrorCode.BUSINESS_LICENSE_EXISTED);
        }

        String businessFilePath = fileStorageService.uploadFile(
                request.getFileData(),
                "/business/registrationFile"
        );

        BusinessLicense license = BusinessLicense.builder()
                .user(user)
                .filePath(businessFilePath)
                .build();

        user.setBusinessLicense(license);
        user.setStatus(Status.WAITING_FOR_APPROVAL);

        // ===== ID CARD =====
        String frontPath = fileStorageService.uploadFile(
                request.getFrontImageData(),
                "/idcard/front"
        );
        String backPath = fileStorageService.uploadFile(
                request.getBackImageData(),
                "/idcard/back"
        );

        IdCardApiRequest frontData = callFptApi(request.getFrontImageData());

        UserIdCard idCard = userIdCardMapper.toUserIdCard(frontData);
        idCard.setUser(user);
        idCard.setFrontImagePath(frontPath);
        idCard.setBackImagePath(backPath);

        // Save all changes
        userIdCardRepository.save(idCard);
        userRepository.save(user);
    }


    public BusinessUploadStatusResponse getBusinessUploadStatusByEmail(String email) {
        var response = BusinessUploadStatusResponse.builder()
                .uploaded(false)
                .build();

        // Check existence via repositories
        String licensePath = businessLicenseRepository.findFilePathByUserEmail(email);
        String frontPath = userIdCardRepository.findFrontImagePathByUserEmail(email);
        String backPath = userIdCardRepository.findBackImagePathByUserEmail(email);

        boolean hasAny = (licensePath != null && !licensePath.isEmpty())
                || (frontPath != null && !frontPath.isEmpty())
                || (backPath != null && !backPath.isEmpty());

        if (hasAny) {
            response.setUploaded(true);
            response.setBusinessLicenseFileName(extractFileName(licensePath));
            response.setIdCardFrontFileName(extractFileName(frontPath));
            response.setIdCardBackFileName(extractFileName(backPath));
        }

        return response;
    }

    private String extractFileName(String path) {
        if (path == null)
            return null;
        int idx = path.lastIndexOf('/') >= 0 ? path.lastIndexOf('/') : path.lastIndexOf('\\');
        return idx >= 0 ? path.substring(idx + 1) : path;
    }

    private IdCardApiRequest callFptApi(MultipartFile file) throws Exception {
        RestTemplate restTemplate = new RestTemplate();

        ByteArrayResource fileResource = new ByteArrayResource(file.getBytes()) {
            @Override
            public String getFilename() {
                return file.getOriginalFilename();
            }
        };

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("image", fileResource);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.add("api_key", API_KEY);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                API_URL,
                HttpMethod.POST,
                requestEntity,
                String.class);

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(response.getBody());
        JsonNode data = root.path("data").get(0);

        return mapper.treeToValue(data, IdCardApiRequest.class);
    }


}
