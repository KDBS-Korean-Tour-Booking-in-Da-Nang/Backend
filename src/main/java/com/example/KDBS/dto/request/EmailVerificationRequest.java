package com.example.KDBS.dto.request;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
public class EmailVerificationRequest {
    String email;
    String otpCode;
}
