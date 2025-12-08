package com.example.KDBS.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class ChatGeminiRequest {
    private List<String> history;
    private String message;
}
