package com.example.KDBS.controller;

import com.example.KDBS.dto.request.BusinessLicenseRequest;
import com.example.KDBS.dto.request.UserRegisterRequest;
import com.example.KDBS.dto.response.ApiResponse;
import com.example.KDBS.dto.response.UserResponse;
import com.example.KDBS.service.UserService;
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

    @PostMapping("/register")
    public ApiResponse<String> register(@RequestBody @Valid UserRegisterRequest request) throws IOException {
        return ApiResponse.<String>builder()
                .result(userService.createUser(request))
                .build();
    }

    @GetMapping
    public ApiResponse<List<UserResponse>> getAllUsers(){
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
}
