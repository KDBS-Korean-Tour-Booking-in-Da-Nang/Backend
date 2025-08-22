package com.example.KDBS.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BusinessLicenseRequest {
    String email;
    MultipartFile fileData;
    MultipartFile frontImageData;
    MultipartFile backImageData;
}
