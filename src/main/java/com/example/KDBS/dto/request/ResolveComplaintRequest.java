package com.example.KDBS.dto.request;

import com.example.KDBS.enums.ComplaintResolutionType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ResolveComplaintRequest {

    @NotNull
    private ComplaintResolutionType resolutionType;

    private String note;
}


