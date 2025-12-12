package com.example.KDBS.controller;

import com.example.KDBS.dto.request.ChatGeminiRequest;
import com.example.KDBS.dto.request.TranslateRequest;
import com.example.KDBS.service.GeminiService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/gemini")
@RequiredArgsConstructor
public class GeminiController {
    private final GeminiService geminiService;

    @PostMapping("/translate")
    public String translateText(@RequestBody TranslateRequest request) {
        return geminiService.translateText(request.getText());
    }

    @PostMapping("/chat")
    public String chat(@RequestBody ChatGeminiRequest request) {
        return geminiService.chatSession(
                request.getHistory(),
                request.getMessage()
        );
    }

}
