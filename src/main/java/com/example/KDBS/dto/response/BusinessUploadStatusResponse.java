package com.example.KDBS.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BusinessUploadStatusResponse {
    private boolean uploaded;
    private String businessLicenseFileName;
    private String idCardFrontFileName;
    private String idCardBackFileName;
}
