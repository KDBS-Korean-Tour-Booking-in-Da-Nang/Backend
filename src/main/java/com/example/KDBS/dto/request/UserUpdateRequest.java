package com.example.KDBS.dto.request;

import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserUpdateRequest {

    @Size(max = 30)
    private String username;

    @Size(max = 11)
    private String phone;

    private LocalDate dob;

    @Size(max = 3)
    private String gender;

    @Size(max = 12)
    private String cccd;

}
