package com.example.KDBS.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "post_hashtags")
public class PostHashtag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_hashtag_id")
    private Long postHashtagId;

    // Belongs to a ForumPost
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    @JsonBackReference
    private ForumPost forumPost;

    // Belongs to a ForumHashtag
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hastag_id", nullable = false)
    @JsonBackReference
    private ForumHashtag hashtag;
}
