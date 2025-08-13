package com.example.KDBS.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Map;

@Getter
@Setter
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class IdCardApiResponse {
    private String id;
    private String id_prob;
    private String name;
    private String name_prob;
    private String dob;
    private String dob_prob;
    private String sex;
    private String sex_prob;
    private String nationality;
    private String nationality_prob;
    private String home;
    private String home_prob;
    private String address;
    private String address_prob;
    private Map<String, String> address_entities;
    private String doe;
    private String doe_prob;
    private String type;
}
