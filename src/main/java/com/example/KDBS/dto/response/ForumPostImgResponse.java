package com.example.KDBS.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ForumPostImgResponse {
    private Long postImgId;
    private String imgPath;
}
