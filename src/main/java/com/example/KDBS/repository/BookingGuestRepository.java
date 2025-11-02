package com.example.KDBS.repository;

import com.example.KDBS.model.BookingGuest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingGuestRepository extends JpaRepository<BookingGuest, Long> {
    // Dùng quan hệ Booking -> bookingId
    List<BookingGuest> findByBooking_BookingId(Long bookingId);
}
