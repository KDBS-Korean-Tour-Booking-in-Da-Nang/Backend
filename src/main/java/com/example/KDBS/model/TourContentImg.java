package com.example.KDBS.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tour_content_img")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class TourContentImg {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tour_img_id")
    private Long tourImgId;

    @Column(name = "img_path", length = 500)
    private String imgPath;

    @ManyToOne
    @JoinColumn(name = "tour_content_id", nullable = false)
    @JsonBackReference
    private TourContent tourContent;
}
