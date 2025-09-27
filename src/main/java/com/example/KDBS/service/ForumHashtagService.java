package com.example.KDBS.service;

import com.example.KDBS.dto.response.HashtagStatsResponse;
import com.example.KDBS.repository.ForumHashtagRepository;
import com.example.KDBS.repository.PostHashtagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ForumHashtagService {

    @Autowired
    private ForumHashtagRepository forumHashtagRepository;

    @Autowired
    private PostHashtagRepository postHashtagRepository;

    @Transactional(readOnly = true)
    public List<HashtagStatsResponse> getPopularHashtags(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        List<Object[]> results = forumHashtagRepository.findPopularHashtags(pageable);
        
        return results.stream()
                .map(result -> HashtagStatsResponse.builder()
                        .content((String) result[0])
                        .postCount((Long) result[1])
                        .totalReactions((Long) result[2])
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<HashtagStatsResponse> searchHashtags(String keyword, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        List<Object[]> results = forumHashtagRepository.searchHashtags(keyword, pageable);
        
        return results.stream()
                .map(result -> HashtagStatsResponse.builder()
                        .content((String) result[0])
                        .postCount((Long) result[1])
                        .totalReactions((Long) result[2])
                        .build())
                .collect(Collectors.toList());
    }
}
