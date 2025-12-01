package com.example.KDBS.controller;

import com.example.KDBS.dto.request.TranslateRequest;
import com.example.KDBS.service.GeminiService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/gemini")
@RequiredArgsConstructor
public class GeminiController {
    private final GeminiService geminiService;

    //Chỉ dùng để test Gemini API
    @PostMapping("/ask")
    public String askGemini(@RequestBody String prompt) {
        return geminiService.askGemini(prompt);
    }

    @PostMapping("/translate")
    public String translateText(@RequestBody TranslateRequest request) {
        return geminiService.translateText(request.getText());
    }
}
