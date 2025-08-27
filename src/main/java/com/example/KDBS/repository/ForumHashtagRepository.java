package com.example.KDBS.repository;

import com.example.KDBS.model.ForumHashtag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ForumHashtagRepository extends JpaRepository<ForumHashtag, Long> {
    Optional<ForumHashtag> findByContent(String content);
    List<ForumHashtag> findByContentIn(List<String> contents);
}