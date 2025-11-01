package com.example.KDBS.service;

import com.example.KDBS.dto.request.BusinessLicenseRequest;
import com.example.KDBS.dto.request.UserRegisterRequest;
import com.example.KDBS.dto.request.UserUpdateRequest;
import com.example.KDBS.dto.response.BusinessUploadStatusResponse;
import com.example.KDBS.dto.request.IdCardApiRequest;
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
import com.example.KDBS.utils.FileUtils;
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
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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

    private static final String API_URL = "https://api.fpt.ai/vision/idr/vnm";
    private static final String API_KEY = "0Ka4zpceIGAxLIlQ1f89RIaXbLaSHSVd";
    @Value("${file.upload-dir}")
    private String uploadDir;

    public String createUser(UserRegisterRequest request) {
        Optional<User> existingByEmail = userRepository.findByEmail(request.getEmail());
        Optional<User> existingByUsername = userRepository.findByUsername(request.getUsername());

        if (existingByUsername.isPresent()) {
            throw new AppException(ErrorCode.USERNAME_EXISTED);
        }

        if (existingByEmail.isPresent()) {
            User user = existingByEmail.get();

            if (user.getStatus() == Status.UNVERIFIED) {
                // If account created within 3 days → resend OTP
                if (user.getCreatedAt().isAfter(LocalDateTime.now().minusDays(3))) {
                    otpService.generateAndSendOTP(user.getEmail(), OTPPurpose.VERIFY_EMAIL);
                    return "Email already registered but not verified. OTP resent.";
                } else {
                    // If expired → delete old unverified user
                    userRepository.delete(user);
                }
            } else {
                throw new AppException(ErrorCode.EMAIL_EXISTED);
            }
        }


        // Tạo user mới với status UNVERIFIED
        User user = userMapper.toUser(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        Role role;
        try {
            role = Role.valueOf(request.getRole().toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            // Nếu request gửi role sai hoặc null, mặc định là USER
            role = Role.USER;
        }
        user.setRole(role);
        user.setStatus(Status.UNVERIFIED);
        user.setCreatedAt(LocalDateTime.now());
        userRepository.save(user);

        otpService.generateAndSendOTP(user.getEmail(), OTPPurpose.VERIFY_EMAIL);

        return "Registration successful. Please check your email for verification code.";
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
        user.setAvatar(FileUtils.convertFileToPath(avatarImg, uploadDir, "/users/avatar"));
        userRepository.save(user);
        return userMapper.toUserResponse(user);
    }

    public List<UserResponse> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(userMapper::toUserResponse)
                .toList();
    }

    public void updateBusinessLicense(BusinessLicenseRequest request) throws IOException {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.EMAIL_NOT_EXISTED));

        if (user.getBusinessLicense() != null) {
            throw new AppException(ErrorCode.BUSINESS_LICENSE_EXISTED);
        }

        String filePath = FileUtils.convertFileToPath(request.getFileData(), uploadDir, "/business/registrationFile");

        // Create new license and link it
        BusinessLicense license = BusinessLicense.builder()
                .user(user)
                .filePath(filePath)
                .build();

        user.setBusinessLicense(license);
        user.setStatus(Status.UNBANNED);
        userRepository.save(user);
    }

    public void processAndSaveIdCard(BusinessLicenseRequest request) throws Exception {

        String frontPath = FileUtils.convertFileToPath(request.getFrontImageData(), uploadDir, "/idcard/front");
        String backPath = FileUtils.convertFileToPath(request.getBackImageData(), uploadDir, "/idcard/back");

        IdCardApiRequest frontData = callFptApi(request.getFrontImageData());

        // Use mapper
        UserIdCard entity = userIdCardMapper.toUserIdCard(frontData);
        entity.setUser(userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND)));
        entity.setFrontImagePath(frontPath);
        entity.setBackImagePath(backPath);

        userIdCardRepository.save(entity);
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
