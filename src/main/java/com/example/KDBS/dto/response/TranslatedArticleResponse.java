package com.example.KDBS.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
public class TranslatedArticleResponse {
    private String articleTitleEN;
    private String articleDescriptionEN;
    private String articleContentEN;
    private String articleTitleKR;
    private String articleDescriptionKR;
    private String articleContentKR;
}
