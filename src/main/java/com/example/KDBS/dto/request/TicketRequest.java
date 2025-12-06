package com.example.KDBS.dto.request;

import com.example.KDBS.enums.TicketReasonType;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class TicketRequest {
    private int userId;
    private String message;
    private List<TicketReasonType> reasons;
}
