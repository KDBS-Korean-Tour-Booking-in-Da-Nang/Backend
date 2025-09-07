package com.example.KDBS.service;

import com.example.KDBS.dto.response.UserSuggestionResponse;
import com.example.KDBS.model.User;
import com.example.KDBS.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserSuggestionService {

    @Autowired
    private UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<UserSuggestionResponse> getSuggestedUsers(int limit) {
        // For now, return random users as suggestions
        // TODO: Implement more sophisticated suggestion algorithm
        List<User> users = userRepository.findRandomUsers(limit);
        
        return users.stream()
                .map(user -> UserSuggestionResponse.builder()
                        .userId(user.getUserId())
                        .username(user.getUsername())
                        .avatar(user.getAvatar())
                        .mutualFriends(0) // TODO: Implement mutual friends calculation
                        .isOnline(false) // TODO: Implement online status
                        .build())
                .collect(Collectors.toList());
    }
}
