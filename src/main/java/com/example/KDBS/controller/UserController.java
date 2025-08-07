package com.example.KDBS.controller;

import com.example.KDBS.dto.request.UserRegisterRequest;
import com.example.KDBS.dto.response.ApiResponse;
import com.example.KDBS.dto.response.UserResponse;
import com.example.KDBS.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin("*")
public class UserController {
    @Autowired
    private UserService userService;

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
}
