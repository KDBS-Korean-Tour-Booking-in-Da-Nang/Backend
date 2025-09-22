package com.example.KDBS.controller;

import com.example.KDBS.dto.request.BusinessLicenseRequest;
import com.example.KDBS.dto.request.UserRegisterRequest;
import com.example.KDBS.dto.request.EmailVerificationRequest;
import com.example.KDBS.dto.response.ApiResponse;
import com.example.KDBS.dto.response.UserResponse;
import com.example.KDBS.dto.response.BusinessUploadStatusResponse;
import com.example.KDBS.dto.response.UserSuggestionResponse;
import com.example.KDBS.enums.OTPPurpose;
import com.example.KDBS.enums.Status;
import com.example.KDBS.exception.AppException;
import com.example.KDBS.exception.ErrorCode;
import com.example.KDBS.model.User;
import com.example.KDBS.repository.UserRepository;
import com.example.KDBS.service.UserService;
import com.example.KDBS.service.OTPService;
import com.example.KDBS.service.UserSuggestionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin("*")
public class UserController {
    @Autowired
    private UserService userService;

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Autowired
    private OTPService otpService;

    @Autowired
    private UserSuggestionService userSuggestionService;
    @Autowired
    private UserRepository userRepository;

    @PostMapping("/register")
    public ApiResponse<String> register(@RequestBody @Valid UserRegisterRequest request) throws IOException {
        return ApiResponse.<String>builder()
                .result(userService.createUser(request))
                .build();
    }

    @PostMapping("/sendOTP")
    public ApiResponse<Void> sendOTP(@RequestBody @Valid EmailVerificationRequest request) {
        try {
            otpService.generateAndSendOTP(request.getEmail(), OTPPurpose.VERIFY_EMAIL);
            return ApiResponse.<Void>builder()
                    .message("OTP sent successfully to your email")
                    .build();
        } catch (Exception e) {
            return ApiResponse.<Void>builder()
                    .message("Failed to send OTP: " + e.getMessage())
                    .build();
        }
    }

    @PostMapping("/verify-email")
    public ApiResponse<Boolean> verifyEmail(@RequestBody @Valid EmailVerificationRequest request) {
        try {
            boolean isValid = otpService.verifyOTP(request.getEmail(), request.getOtpCode(), OTPPurpose.VERIFY_EMAIL);

            if (isValid) {
                // Update status = UNBANNED
                User user = userRepository.findByEmail(request.getEmail())
                        .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

                user.setStatus(Status.UNBANNED);
                userRepository.save(user);
            }

            return ApiResponse.<Boolean>builder()
                    .result(isValid)
                    .message(isValid ? "Email verified successfully" : "Invalid OTP")
                    .build();
        } catch (Exception e) {
            return ApiResponse.<Boolean>builder()
                    .result(false)
                    .message("Failed to verify email: " + e.getMessage())
                    .build();
        }
    }


    @PostMapping("/regenerate-otp")
    public ApiResponse<Void> regenerateOTP(@RequestBody @Valid EmailVerificationRequest request) {
        try {
            otpService.generateAndSendOTP(request.getEmail(), OTPPurpose.VERIFY_EMAIL);
            return ApiResponse.<Void>builder()
                    .message("New OTP sent successfully to your email")
                    .build();
        } catch (Exception e) {
            return ApiResponse.<Void>builder()
                    .message("Failed to regenerate OTP: " + e.getMessage())
                    .build();
        }
    }

    @GetMapping
    public ApiResponse<List<UserResponse>> getAllUsers() {
        return ApiResponse.<List<UserResponse>>builder()
                .result(userService.getAllUsers())
                .build();
    }

    @PutMapping(path = "/update-business-license", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> updateBusinessLicense(
            @RequestPart("file") MultipartFile file,
            @RequestPart("idCardFront") MultipartFile idCardFront,
            @RequestPart("idCardBack") MultipartFile idCardBack,
            @RequestParam("email") String email) throws Exception {

        // Build request DTO
        BusinessLicenseRequest request = BusinessLicenseRequest.builder()
                .email(email)
                .fileData(file)
                .frontImageData(idCardFront)
                .backImageData(idCardBack)
                .build();

        userService.updateBusinessLicense(request);
        userService.processAndSaveIdCard(request);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/business-upload-status")
    public ApiResponse<BusinessUploadStatusResponse> getBusinessUploadStatus(@RequestParam("email") String email) {
        BusinessUploadStatusResponse status = userService.getBusinessUploadStatusByEmail(email);
        return ApiResponse.<BusinessUploadStatusResponse>builder()
                .result(status)
                .build();
    }

    @GetMapping("/suggestions")
    public ApiResponse<List<UserSuggestionResponse>> getSuggestedUsers(
            @RequestParam(defaultValue = "5") int limit) {
        List<UserSuggestionResponse> suggestions = userSuggestionService.getSuggestedUsers(limit);
        return ApiResponse.<List<UserSuggestionResponse>>builder()
                .result(suggestions)
                .build();
    }
}
