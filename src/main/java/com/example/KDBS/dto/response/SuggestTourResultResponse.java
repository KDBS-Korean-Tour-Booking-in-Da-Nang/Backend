package com.example.KDBS.dto.response;

import lombok.Data;
import java.util.List;

@Data
public class SuggestTourResultResponse {
    private List<Long> recommendedTourIds;
}
