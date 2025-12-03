package com.example.KDBS.dto.response;

import com.example.KDBS.enums.StaffTask;
import com.example.KDBS.enums.Status;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
public class UserResponse {
    private int userId;
    private String username;
    private String email;
    private String password;
    private String avatar;
    private String phone;
    private LocalDate dob;
    private String address;
    private String cccd;
    private BigDecimal balance;
    private String gender;
    private LocalDateTime createdAt;
    private String status;
    private String role;
    private StaffTask staffTask;
    private String banReason;

}
