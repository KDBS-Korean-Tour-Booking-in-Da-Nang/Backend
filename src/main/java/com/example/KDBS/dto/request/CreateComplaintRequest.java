package com.example.KDBS.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateComplaintRequest {

    @NotBlank
    private String message;
}


