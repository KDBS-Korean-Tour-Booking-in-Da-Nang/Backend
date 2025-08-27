package com.example.KDBS.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class HashtagResponse {
    private Long hashtagId;
    private String content;
}
