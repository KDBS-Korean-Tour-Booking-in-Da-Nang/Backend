package com.example.KDBS.dto.response;

import com.example.KDBS.enums.TourStatus;
import lombok.*;

import java.util.Map;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CompanyTourStatisticResponse {

    private long totalTours;

    private Map<TourStatus, Long> byStatus;
}
