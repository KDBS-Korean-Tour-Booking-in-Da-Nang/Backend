package com.example.KDBS.service;

import com.example.KDBS.dto.response.BookingSummaryResponse;
import com.example.KDBS.dto.response.TourDeleteRequestResponse;
import com.example.KDBS.enums.BookingStatus;
import com.example.KDBS.enums.TourDeleteStatus;
import com.example.KDBS.enums.TourStatus;
import com.example.KDBS.exception.AppException;
import com.example.KDBS.exception.ErrorCode;
import com.example.KDBS.mapper.BookingSimpleMapper;
import com.example.KDBS.mapper.TourDeleteMapper;
import com.example.KDBS.model.Booking;
import com.example.KDBS.model.Tour;
import com.example.KDBS.model.TourDeleteRequest;
import com.example.KDBS.repository.BookingRepository;
import com.example.KDBS.repository.TourDeleteRequestRepository;
import com.example.KDBS.repository.TourRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TourDeleteService {

    private final TourRepository tourRepository;
    private final BookingRepository bookingRepository;
    private final TourDeleteRequestRepository deleteRepo;
    private final TourDeleteMapper deleteMapper;
    private final BookingSimpleMapper bookingSimpleMapper;

    @Transactional
    public TourDeleteRequestResponse createDeleteRequest(Long tourId, String note) {
        Tour tour = tourRepository.findById(tourId)
                .orElseThrow(() -> new AppException(ErrorCode.TOUR_NOT_FOUND));

        TourDeleteRequest req = TourDeleteRequest.builder()
                .tour(tour)
                .companyNote(note)
                .status(TourDeleteStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        deleteRepo.save(req);

        // Convert entity → response
        TourDeleteRequestResponse res = deleteMapper.toResponse(req);

        // Add booking info
        var bookings = bookingRepository.findByTour_TourIdOrderByCreatedAtDesc(tourId)
                .stream()
                .map(bookingSimpleMapper::toSimple)
                .toList();

        res.setBookings(bookings);
        res.setBookingCount(bookings.size());

        return res;
    }

    public List<TourDeleteRequestResponse> getPendingRequests() {
        return deleteRepo.findByStatus(TourDeleteStatus.PENDING)
                .stream()
                .map(req -> {
                    var res = deleteMapper.toResponse(req);

                    var bookings = bookingRepository
                            .findByTour_TourIdOrderByCreatedAtDesc(req.getTour().getTourId())
                            .stream()
                            .map(bookingSimpleMapper::toSimple)
                            .toList();

                    res.setBookings(bookings);
                    res.setBookingCount(bookings.size());

                    return res;
                })
                .toList();
    }

    @Transactional
    public void approve(Long id, String staffNote) {
        TourDeleteRequest req = deleteRepo.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.DELETE_REQUEST_NOT_FOUND));

        req.setStatus(TourDeleteStatus.APPROVED);
        req.setStaffNote(staffNote);
        req.setReviewedAt(LocalDateTime.now());

        // Disable tour
        Tour tour = req.getTour();
        tour.setTourStatus(TourStatus.DISABLED);
    }

    @Transactional
    public void reject(Long id, String staffNote) {
        TourDeleteRequest req = deleteRepo.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.DELETE_REQUEST_NOT_FOUND));

        req.setStatus(TourDeleteStatus.REJECTED);
        req.setStaffNote(staffNote);
        req.setReviewedAt(LocalDateTime.now());
    }
}
