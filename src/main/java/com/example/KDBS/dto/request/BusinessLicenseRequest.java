package com.example.KDBS.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
    @NotBlank(message = "Company email is required")
    @Size(max = 100, message = "Company email must not exceed 100 characters")
    @Email(message = "Invalid email format")
    String email;
    @NotNull(message = "File data is required")
    MultipartFile fileData;
    @NotNull(message = "Front image data is required")
    MultipartFile frontImageData;
    @NotNull(message = "Back image data is required")
    MultipartFile backImageData;
}
