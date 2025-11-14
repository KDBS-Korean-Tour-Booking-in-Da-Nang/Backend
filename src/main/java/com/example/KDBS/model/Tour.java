package com.example.KDBS.model;

import com.example.KDBS.enums.TourStatus;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "tours")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tour {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tour_id")
    private Long tourId;

    @Column(name = "company_id")
    private int companyId;

    @Column(name = "tour_name")
    private String tourName;

    @Lob
    @Column(name = "tour_description", columnDefinition = "MEDIUMTEXT")
    private String tourDescription;

    @Column(name = "tour_img_path")
    private String tourImgPath;

    @Column(name = "tour_duration")
    private String tourDuration;

    @Column(name = "tour_int_duration")
    private int tourIntDuration;

    @Column(name = "tour_expiration_date")
    private LocalDate tourExpirationDate;

    @Column(name = "tour_deadline")
    private int tourDeadline;

    @Column(name = "tour_departure_point")
    private String tourDeparturePoint;

    @Column(name = "tour_vehicle")
    private String tourVehicle;

    @Column(name = "tour_type")
    private String tourType;

    // Store potentially long JSON/stringified schedule
    @Column(name = "tour_schedule", columnDefinition = "TEXT")
    private String tourSchedule;

    private int amount;

    @Column(name = "adult_price", precision = 10, scale = 2)
    private BigDecimal adultPrice;

    @Column(name = "children_price", precision = 10, scale = 2)
    private BigDecimal childrenPrice;

    @Column(name = "baby_price", precision = 10, scale = 2)
    private BigDecimal babyPrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "tour_status", length = 50)
    private TourStatus tourStatus; // ✅ default value

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // Policies are consolidated into tourDescription. Legacy columns removed.

    @PrePersist
    protected void onCreate() {
        this.tourStatus = TourStatus.NOT_APPROVED;
        this.createdAt = LocalDateTime.now();
    }

    // Relationships
    @OneToMany(mappedBy = "tour", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<TourContent> contents;

    @OneToMany(mappedBy = "tour", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<TourRated> ratings;
}
