package com.example.KDBS.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "tour_rated",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"tour_id", "user_id"}) // ✅ đảm bảo 1 user chỉ đánh giá 1 tour
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TourRated {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tour_rated_id")
    private Long tourRatedId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonBackReference
    private User user;

    @ManyToOne
    @JoinColumn(name = "tour_id", nullable = false)
    @JsonBackReference
    private Tour tour;

    private Integer star;

    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

}
