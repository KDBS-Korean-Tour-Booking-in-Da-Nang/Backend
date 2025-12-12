package com.example.KDBS.dto.response;

import com.example.KDBS.enums.Status;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class UserStatisticResponse {

    private long totalUsers;

    private Map<Status, Long> byStatus;
}