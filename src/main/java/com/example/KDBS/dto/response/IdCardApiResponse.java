package com.example.KDBS.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Map;

@Getter
@Setter
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonIgnoreProperties(ignoreUnknown = true) // Ignore unknown JSON fields
public class IdCardApiResponse {

    @JsonProperty("id")
    private String idNumber;

    @JsonProperty("id_prob")
    private String idProb;

    private String name;

    @JsonProperty("name_prob")
    private String nameProb;

    private String dob;

    @JsonProperty("dob_prob")
    private String dobProb;

    private String sex;

    @JsonProperty("sex_prob")
    private String sexProb;

    private String nationality;

    @JsonProperty("nationality_prob")
    private String nationalityProb;

    private String home;

    @JsonProperty("home_prob")
    private String homeProb;

    private String address;

    @JsonProperty("address_prob")
    private String addressProb;

    @JsonProperty("address_entities")
    private Map<String, String> addressEntities;

    private String doe;

    @JsonProperty("doe_prob")
    private String doeProb;

    private String type;

    // New fields from API
    @JsonProperty("overall_score")
    private String overallScore;

    @JsonProperty("number_of_name_lines")
    private String numberOfNameLines;

    @JsonProperty("type_new")
    private String typeNew;
}
