package com.example.KDBS.repository;

import com.example.KDBS.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    
    List<Booking> findByContactEmailOrderByCreatedAtDesc(String contactEmail);
    
    List<Booking> findByTourIdOrderByCreatedAtDesc(Long tourId);
    
    @Query("SELECT b FROM Booking b WHERE b.departureDate = :departureDate")
    List<Booking> findByDepartureDate(@Param("departureDate") LocalDate departureDate);
    
    @Query("SELECT b FROM Booking b WHERE b.tourId = :tourId AND b.departureDate = :departureDate")
    List<Booking> findByTourIdAndDepartureDate(@Param("tourId") Long tourId, @Param("departureDate") LocalDate departureDate);
    
    @Query("SELECT b FROM Booking b JOIN b.guests g WHERE g.idNumber = :idNumber")
    List<Booking> findByGuestIdNumber(@Param("idNumber") String idNumber);
    
    Optional<Booking> findByBookingId(Long bookingId);

    @Query("SELECT b FROM Booking b LEFT JOIN FETCH b.guests WHERE b.bookingId = :bookingId")
    Optional<Booking> findByIdWithGuests(@Param("bookingId") Long bookingId);

}
