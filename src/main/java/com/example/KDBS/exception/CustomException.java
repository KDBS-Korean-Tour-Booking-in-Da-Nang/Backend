package com.example.KDBS.exception;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CustomException extends RuntimeException {
    private final String message;

    @Override
    public String getMessage() {
        return message;
    }
}
