package com.example.KDBS.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSuggestionResponse {
    private int userId;
    private String username;
    private String avatar;
    private Integer mutualFriends;
    private Boolean isOnline;
}
