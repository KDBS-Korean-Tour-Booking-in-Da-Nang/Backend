package com.example.KDBS.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "forum_hashtags")
public class ForumHashtag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "hastag_id")
    private Long hashtagId;

    @Column(length = 255, nullable = false, unique = true)
    private String content;

    @OneToMany(mappedBy = "hashtag", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<PostHashtag> postHashtags;
}
