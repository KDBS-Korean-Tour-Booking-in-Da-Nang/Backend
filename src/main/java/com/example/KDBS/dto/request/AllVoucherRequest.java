package com.example.KDBS.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AllVoucherRequest {
    private long tourId;
    private int adultsCount;
    private int childrenCount;
    private int babiesCount;
}
