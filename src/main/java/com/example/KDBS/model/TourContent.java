package com.example.KDBS.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tour_content")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TourContent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tour_content_id")
    private Long tourContentId;

    @Column(name = "tour_content_title", columnDefinition = "LONGTEXT")
    private String tourContentTitle; // HTML rich text

    @Column(name = "tour_content_description", columnDefinition = "LONGTEXT")
    private String tourContentDescription; // HTML rich text

    // Optional presentation preferences for rendering on FE
    @Column(name = "day_color", length = 20)
    private String dayColor;

    @Column(name = "title_alignment", length = 10)
    private String titleAlignment; // left | center | right

    @ManyToOne
    @JoinColumn(name = "tour_id", nullable = false)
    @JsonBackReference
    private Tour tour;

    @OneToMany(mappedBy = "tourContent", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<TourContentImg> images = new ArrayList<>();
}
