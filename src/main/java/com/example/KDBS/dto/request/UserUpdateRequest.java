package com.example.KDBS.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserUpdateRequest {
    @NotBlank(message = "Username is required")
    @Size(max = 100, message = "Username must not exceed 100 characters")
    private String username;

    @Size(max = 20, message = "Phone number must not exceed 20 characters")
    private String phone;

    private LocalDate dob;

    @Size(max = 3, message = "Gender must not exceed 3 characters")
    private String gender;
}
