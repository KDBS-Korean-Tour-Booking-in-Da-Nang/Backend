package com.example.KDBS.repository;

import com.example.KDBS.model.BookingComplaint;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookingComplaintRepository extends JpaRepository<BookingComplaint, Long> {
    List<BookingComplaint> findByBooking_BookingId(Long bookingId);
}


