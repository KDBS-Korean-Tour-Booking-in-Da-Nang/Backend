package com.example.KDBS.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
public class BanUserRequest {
    private boolean ban;
    private String banReason; // nullable nếu unban
}
