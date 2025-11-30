package com.example.KDBS.controller;

import com.example.KDBS.service.GeminiService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/gemini")
@RequiredArgsConstructor
public class GeminiController {
    private final GeminiService geminiService;

    //Chỉ dùng để test Gemini API
    @GetMapping("/ask")
    public String askGemini(@RequestBody String prompt) {
        return geminiService.askGemini(prompt);
    }

    @GetMapping("/translate")
    public String translateText(@RequestBody String text) {
        return geminiService.translateText(text);
    }
}
