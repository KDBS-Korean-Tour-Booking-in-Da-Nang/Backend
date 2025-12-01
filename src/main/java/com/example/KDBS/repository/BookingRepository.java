package com.example.KDBS.repository;

import com.example.KDBS.enums.BookingStatus;
import com.example.KDBS.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByUserEmailOrderByCreatedAtDesc(String contactEmail);
    
    List<Booking> findByTour_TourIdOrderByCreatedAtDesc(Long tourId);

    List<Booking> findByTour_CompanyIdOrderByCreatedAtDesc(int tour_companyId);

    List<Booking> findByBookingStatusIn(List<BookingStatus> bookingStatuses);

    @Query("SELECT b FROM Booking b LEFT JOIN FETCH b.guests WHERE b.bookingId = :bookingId")
    Optional<Booking> findByIdWithGuests(@Param("bookingId") Long bookingId);

    Boolean existsByTour_TourId(Long tourTourId);
}
