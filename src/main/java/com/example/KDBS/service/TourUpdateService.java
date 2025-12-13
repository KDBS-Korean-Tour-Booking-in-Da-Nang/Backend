package com.example.KDBS.service;

import com.example.KDBS.dto.request.TourRequest;
import com.example.KDBS.dto.request.TourUpdateCreateRequest;
import com.example.KDBS.dto.response.BookingSummaryResponse;
import com.example.KDBS.dto.response.TourResponse;
import com.example.KDBS.dto.response.TourUpdateRequestResponse;
import com.example.KDBS.enums.StaffTask;
import com.example.KDBS.enums.TourUpdateStatus;
import com.example.KDBS.exception.AppException;
import com.example.KDBS.exception.ErrorCode;
import com.example.KDBS.mapper.BookingSimpleMapper;
import com.example.KDBS.mapper.TourUpdateMapper;
import com.example.KDBS.model.Booking;
import com.example.KDBS.model.Tour;
import com.example.KDBS.model.TourUpdateRequest;
import com.example.KDBS.repository.BookingRepository;
import com.example.KDBS.repository.TourRepository;
import com.example.KDBS.repository.TourUpdateRequestRepository;
import com.example.KDBS.utils.FileStorageService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.json.Json;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TourUpdateService {

    private final TourRepository tourRepository;
    private final TourUpdateRequestRepository updateRepo;
    private final ObjectMapper objectMapper;
    private final FileStorageService fileStorageService;
    private final TourUpdateMapper updateMapper;
    private final TourService tourService;
    private final BookingSimpleMapper bookingSimpleMapper;
    private final BookingRepository bookingRepository;
    private final StaffService staffService;


    @Transactional
    public TourUpdateRequestResponse createUpdateRequest(Long tourId,
                                                         TourUpdateCreateRequest req,
                                                         MultipartFile tourImg) throws Exception {

        Tour original = tourRepository.findByIdWithContents(tourId)
                .orElseThrow(() -> new AppException(ErrorCode.TOUR_NOT_FOUND));

        // Convert updated tour to JSON
        String jsonUpdatedTour = objectMapper.writeValueAsString(req.getUpdatedTour());

        // Save new tour image (optional)
        String imgPath = null;
        if (tourImg != null && !tourImg.isEmpty()) {
            imgPath = fileStorageService.uploadFile(tourImg, "/tours/thumbnails");
        }

        TourUpdateRequest update = TourUpdateRequest.builder()
                .originalTour(original)
                .updatedTourJson(jsonUpdatedTour)
                .updatedImagePath(imgPath)
                .companyNote(req.getNote())
                .status(TourUpdateStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        updateRepo.save(update);

        TourUpdateRequestResponse res = updateMapper.toResponse(update);
        res.setUpdatedTour(req.getUpdatedTour());

        // Attach booking list (Simple format)
        var bookings = bookingRepository.findByTour_TourIdOrderByCreatedAtDesc(original.getTourId())
                .stream()
                .map(bookingSimpleMapper::toSimple)
                .toList();

        res.setBookings(bookings);
        res.setBookingCount(bookings.size());

        return res;
    }


    @Transactional
    public TourResponse approveRequest(Long requestId, String staffNote) throws Exception {
        staffService.getAuthorizedStaff(StaffTask.APPROVE_TOUR_BOOKING_AND_APPROVE_ARTICLE);

        TourUpdateRequest req = updateRepo.findById(requestId)
                .orElseThrow(() -> new AppException(ErrorCode.UPDATE_REQUEST_NOT_FOUND));

        if (req.getStatus() != TourUpdateStatus.PENDING)
            throw new AppException(ErrorCode.INVALID_UPDATE_STATE);

        // Convert JSON → TourRequest
        TourRequest updatedRequest =
                objectMapper.readValue(req.getUpdatedTourJson(), TourRequest.class);

        // Apply approved update
        TourResponse updatedTour = tourService.applyApprovedUpdate(
                req.getOriginalTour(),
                updatedRequest,
                req.getUpdatedImagePath()
        );

        // Update request state
        req.setStatus(TourUpdateStatus.APPROVED);
        req.setStaffNote(staffNote);
        req.setReviewedAt(LocalDateTime.now());

        return updatedTour;
    }


    @Transactional
    public void rejectRequest(Long requestId, String staffNote) {
        staffService.getAuthorizedStaff(StaffTask.APPROVE_TOUR_BOOKING_AND_APPROVE_ARTICLE);

        TourUpdateRequest req = updateRepo.findById(requestId)
                .orElseThrow(() -> new AppException(ErrorCode.UPDATE_REQUEST_NOT_FOUND));

        req.setStatus(TourUpdateStatus.REJECTED);
        req.setStaffNote(staffNote);
        req.setReviewedAt(LocalDateTime.now());
    }


    public List<TourUpdateRequestResponse> getPendingRequests() {
        return updateRepo.findByStatus(TourUpdateStatus.PENDING)
                .stream()
                .map(update -> {
                    var res = updateMapper.toResponse(update);

                    // Add booking list to each request
                    var bookings = bookingRepository.findByTour_TourIdOrderByCreatedAtDesc(update.getOriginalTour().getTourId())
                            .stream()
                            .map(bookingSimpleMapper::toSimple)
                            .toList();

                    res.setBookings(bookings);
                    res.setBookingCount(bookings.size());
                    try {
                        TourRequest tourRequest = objectMapper.readValue(update.getUpdatedTourJson(), TourRequest.class);
                        res.setUpdatedTour(tourRequest);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }

                    return res;
                })
                .toList();
    }

}
