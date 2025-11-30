package com.example.KDBS.controller;

import com.example.KDBS.dto.request.CozeChatRequest;
import com.example.KDBS.model.User;
import com.example.KDBS.repository.UserRepository;
import com.example.KDBS.service.CozeChatService;
import com.example.KDBS.service.UserActionLogService;
import com.example.KDBS.exception.AppException;
import com.example.KDBS.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin("*")
public class CozeChatController {

    private final CozeChatService cozeChatService;
    private final UserRepository userRepository;
    private final UserActionLogService userActionLogService;

    @PostMapping("/chat")
    public ResponseEntity<String> chatWithBot(
            @RequestHeader("User-Email") String email,
            @RequestBody CozeChatRequest request
    ) {
        // 1) Lấy user theo email (giống UserActionLogService)
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        String userId = String.valueOf(user.getUserId()); // hoặc dùng email cũng được

        // 2) Gọi Coze
        String cozeResponse = cozeChatService.chat(userId, request.getMessage());
        return ResponseEntity.ok(cozeResponse);
    }
}
