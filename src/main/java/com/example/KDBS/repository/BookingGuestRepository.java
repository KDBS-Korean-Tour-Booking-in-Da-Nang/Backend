package com.example.KDBS.repository;

import com.example.KDBS.model.BookingGuest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingGuestRepository extends JpaRepository<BookingGuest, Long> {
    
    List<BookingGuest> findByBookingId(Long bookingId);
    
    @Query("SELECT bg FROM BookingGuest bg WHERE bg.bookingId = :bookingId ORDER BY bg.guestType, bg.fullName")
    List<BookingGuest> findByBookingIdOrderByGuestTypeAndName(@Param("bookingId") Long bookingId);
    
    @Query("SELECT bg FROM BookingGuest bg WHERE bg.idNumber = :idNumber")
    List<BookingGuest> findByIdNumber(@Param("idNumber") String idNumber);
    
    @Query("SELECT COUNT(bg) FROM BookingGuest bg WHERE bg.bookingId = :bookingId AND bg.guestType = :guestType")
    Long countByBookingIdAndGuestType(@Param("bookingId") Long bookingId, @Param("guestType") String guestType);
}
