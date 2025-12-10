package com.example.KDBS.model;

import com.example.KDBS.enums.TourDeleteStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Entity
@Table(name = "tour_delete_request")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TourDeleteRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tour_id", nullable = false)
    private Tour tour;

    @Column(columnDefinition = "TEXT")
    private String companyNote;

    @Column(columnDefinition = "TEXT")
    private String staffNote;

    @Enumerated(EnumType.STRING)
    private TourDeleteStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime reviewedAt;
}
