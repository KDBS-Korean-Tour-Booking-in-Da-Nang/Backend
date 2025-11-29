package com.example.KDBS.dto.request;

import com.example.KDBS.enums.StaffTask;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class StaffTaskUpdateRequest {
    @NotBlank(message = "Username is required")
    String username;

    StaffTask staffTask;
}
