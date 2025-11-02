package com.example.KDBS.model;

import com.example.KDBS.enums.BookingStatus;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "bookings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "booking_id")
    private Long bookingId;

    @Column(name = "tour_id", nullable = false)
    private Long tourId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tour_id", insertable = false, updatable = false)
    private Tour tour;

    @Column(name = "user_email", length = 100)
    private String userEmail;

    @Column(name = "booking_status", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private BookingStatus bookingStatus;

    @Column(name = "contact_name", nullable = false, length = 100)
    private String contactName;

    @Column(name = "contact_address", length = 255)
    private String contactAddress;

    @Column(name = "contact_phone", nullable = false, length = 20)
    private String contactPhone;

    @Column(name = "contact_email", length = 100)
    private String contactEmail;

    @Column(name = "pickup_point", length = 255)
    private String pickupPoint;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    @Column(name = "departure_date", nullable = false)
    private LocalDate departureDate;

    @Column(name = "adults_count")
    private Integer adultsCount;

    @Column(name = "children_count")
    private Integer childrenCount;

    @Column(name = "babies_count")
    private Integer babiesCount;

    @Column(name = "total_guests", nullable = false)
    private Integer totalGuests;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<BookingGuest> guests;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.bookingStatus == null) {
            this.bookingStatus = BookingStatus.PENDING;
        }
        this.adultsCount = 1;
        this.childrenCount = 0;
        this.babiesCount = 0;
        // Calculate total guests
        this.totalGuests = this.adultsCount + this.childrenCount + this.babiesCount;
    }
}
