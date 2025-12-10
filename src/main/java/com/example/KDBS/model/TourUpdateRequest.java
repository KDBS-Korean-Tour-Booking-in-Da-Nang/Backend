package com.example.KDBS.model;


import com.example.KDBS.enums.TourUpdateStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "tour_update_request")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TourUpdateRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Tour gốc cần update
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "original_tour_id", nullable = false)
    private Tour originalTour;

    // JSON lưu dữ liệu tour mới
    @Lob
    private String updatedTourJson;

    @Column(name = "updated_image_path")
    private String updatedImagePath;

    // Note của company
    @Column(columnDefinition = "TEXT")
    private String companyNote;

    // Note của staff khi duyệt
    @Column(columnDefinition = "TEXT")
    private String staffNote;

    @Enumerated(EnumType.STRING)
    private TourUpdateStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime reviewedAt;
}
