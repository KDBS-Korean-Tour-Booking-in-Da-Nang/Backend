package com.example.KDBS.service;

import com.example.KDBS.dto.request.BookingRequest;
import com.example.KDBS.dto.response.BookingGuestResponse;
import com.example.KDBS.dto.response.BookingResponse;
import com.example.KDBS.dto.response.BookingSummaryResponse;
import com.example.KDBS.enums.BookingGuestType;
import com.example.KDBS.exception.AppException;
import com.example.KDBS.exception.ErrorCode;
import com.example.KDBS.mapper.BookingMapper;
import com.example.KDBS.model.Booking;
import com.example.KDBS.model.BookingGuest;
import com.example.KDBS.model.Tour;
import com.example.KDBS.model.Transaction;
import com.example.KDBS.repository.BookingGuestRepository;
import com.example.KDBS.repository.BookingRepository;
import com.example.KDBS.repository.TourRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private BookingGuestRepository bookingGuestRepository;
    @Autowired
    private TourRepository tourRepository;
    @Autowired
    private EmailService emailService;
    @Autowired
    private BookingMapper bookingMapper;

    @Transactional
    public BookingResponse createBooking(BookingRequest request) {
        // Validate tour exists
        Tour tour = tourRepository.findById(request.getTourId())
                .orElseThrow(() -> new AppException(ErrorCode.TOUR_NOT_FOUND));

        // Validate guest counts match
        validateGuestCounts(request);

        // Create booking first
        Booking booking = bookingMapper.toBooking(request);

        // Save booking first to get ID
        Booking savedBooking = bookingRepository.save(booking);

        // Map guests và set booking reference
        List<BookingGuest> guests = request.getBookingGuestRequests().stream()
                .map(guestReq -> {
                    BookingGuest guest = bookingMapper.toBookingGuest(guestReq);
                    guest.setBooking(savedBooking);
                    return guest;
                })
                .toList();

        // Save guests
        List<BookingGuest> savedGuests = bookingGuestRepository.saveAll(guests);
        savedBooking.setGuests(savedGuests);

        // Map sang response
        BookingResponse response = bookingMapper.toBookingResponse(savedBooking);
        response.setTourName(tour.getTourName());
        response.setGuests(bookingMapper.toBookingGuestResponses(savedGuests));

        return response;
    }

    @Transactional(readOnly = true)
    public BookingResponse getBookingById(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));

        Tour tour = tourRepository.findById(booking.getTourId())
                .orElseThrow(() -> new AppException(ErrorCode.TOUR_NOT_FOUND));

        // Load guests
        List<BookingGuest> guests = bookingGuestRepository.findByBooking_BookingId(bookingId);
        booking.setGuests(guests);

        BookingResponse response = bookingMapper.toBookingResponse(booking);
        response.setTourName(tour.getTourName());
        response.setGuests(bookingMapper.toBookingGuestResponses(guests));

        return response;
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getBookingsByEmail(String email) {
        List<Booking> bookings = bookingRepository.findByContactEmailOrderByCreatedAtDesc(email);
        return bookings.stream()
                .map(booking -> {
                    Tour tour = tourRepository.findById(booking.getTourId()).orElse(null);
                    List<BookingGuest> guests = bookingGuestRepository.findByBooking_BookingId(booking.getBookingId());
                    booking.setGuests(guests);

                    BookingResponse response = bookingMapper.toBookingResponse(booking);
                    response.setTourName(tour != null ? tour.getTourName() : "Unknown Tour");
                    response.setGuests(bookingMapper.toBookingGuestResponses(guests));
                    return response;
                })
                .toList();
    }


    @Transactional(readOnly = true)
    public List<BookingResponse> getBookingsByTourId(Long tourId) {
        List<Booking> bookings = bookingRepository.findByTourIdOrderByCreatedAtDesc(tourId);
        Tour tour = tourRepository.findById(tourId).orElse(null);

        return bookings.stream()
                .map(booking -> {
                    List<BookingGuest> guests = bookingGuestRepository.findByBooking_BookingId(booking.getBookingId());
                    booking.setGuests(guests);

                    BookingResponse response = bookingMapper.toBookingResponse(booking);
                    response.setTourName(tour != null ? tour.getTourName() : "Unknown Tour");
                    response.setGuests(bookingMapper.toBookingGuestResponses(guests));
                    return response;
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<BookingGuestResponse> getAllGuestsByBookingId(Long bookingId) {
        List<BookingGuest> guests = bookingGuestRepository.findByBooking_BookingId(bookingId);
        return bookingMapper.toBookingGuestResponses(guests);
    }

    @Transactional(readOnly = true)
    public List<BookingSummaryResponse> getBookingSummaryByEmail(String email) {
        List<Booking> bookings = bookingRepository.findByContactEmailOrderByCreatedAtDesc(email);

        return bookings.stream()
                .map(booking -> {
                    Tour tour = tourRepository.findById(booking.getTourId()).orElse(null);
                    String tourName = (tour != null ? tour.getTourName() : "Unknown Tour");
                    BigDecimal totalAmount = calculateBookingTotal(booking.getBookingId());

                    return bookingMapper.toBookingSummaryResponse(booking, tourName, totalAmount);
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public BigDecimal calculateBookingTotal(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));

        Tour tour = tourRepository.findById(booking.getTourId())
                .orElseThrow(() -> new AppException(ErrorCode.TOUR_NOT_FOUND));

        BigDecimal adultTotal = tour.getAdultPrice().multiply(BigDecimal.valueOf(booking.getAdultsCount()));
        BigDecimal childrenTotal = tour.getChildrenPrice().multiply(BigDecimal.valueOf(booking.getChildrenCount()));
        BigDecimal babyTotal = tour.getBabyPrice().multiply(BigDecimal.valueOf(booking.getBabiesCount()));

        return adultTotal.add(childrenTotal).add(babyTotal);
    }

    private void validateGuestCounts(BookingRequest request) {
        int actualAdults = (int) request.getBookingGuestRequests().stream()
                .filter(g -> g.getBookingGuestType() == BookingGuestType.ADULT)
                .count();
        int actualChildren = (int) request.getBookingGuestRequests().stream()
                .filter(g -> g.getBookingGuestType() == BookingGuestType.CHILD)
                .count();
        int actualBabies = (int) request.getBookingGuestRequests().stream()
                .filter(g -> g.getBookingGuestType() == BookingGuestType.BABY)
                .count();

        if (!actualAdultsEquals(request, actualAdults)
                || !actualChildrenEquals(request, actualChildren)
                || !actualBabiesEquals(request, actualBabies)) {
            throw new AppException(ErrorCode.INVALID_GUEST_COUNT);
        }
    }

    private boolean actualAdultsEquals(BookingRequest request, int actualAdults) {
        return actualAdults == (request.getAdultsCount() != null ? request.getAdultsCount() : 0);
    }

    private boolean actualChildrenEquals(BookingRequest request, int actualChildren) {
        return actualChildren == (request.getChildrenCount() != null ? request.getChildrenCount() : 0);
    }

    private boolean actualBabiesEquals(BookingRequest request, int actualBabies) {
        return actualBabies == (request.getBabiesCount() != null ? request.getBabiesCount() : 0);
    }

    /**
     * Gửi email xác nhận booking, for testing purposes
     */
    @Transactional(readOnly = true)
    public void sendBookingConfirmationEmail(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));

        Tour tour = tourRepository.findById(booking.getTourId())
                .orElseThrow(() -> new AppException(ErrorCode.TOUR_NOT_FOUND));

        emailService.sendBookingConfirmationEmailAsync(booking, tour);
        log.info("Booking confirmation email sent manually for booking ID: {}", bookingId);
    }

    /**
     * Gửi email xác nhận booking nếu transaction liên quan đến booking
     */
    public void sendBookingConfirmationEmailIfNeeded(Transaction transaction) {
        try {
            // Extract booking ID from orderInfo if it's a booking payment
            // Format: "Booking payment for booking ID: {bookingId}"
            String orderInfo = transaction.getOrderInfo();
            if (orderInfo != null && orderInfo.contains("Booking payment for booking ID:")) {

                //Get booking ID by parsing orderInfo replace and split then get first part and trim
                //Example: "Booking payment for booking ID: 123 | Tour: Amazing Tour - 2 guests on 2023-10-15"
                String bookingIdStr = orderInfo.replace("Booking payment for booking ID:", "")
                        .split("\\|")[0]
                        .trim();
                Long bookingId = Long.parseLong(bookingIdStr);

                // Get booking and tour information
                Booking booking = bookingRepository.findByIdWithGuests(bookingId).orElse(null);
                if (booking != null) {
                    Tour tour = tourRepository.findById(booking.getTourId()).orElse(null);
                    if (tour != null) {
                        emailService.sendBookingConfirmationEmailAsync(booking, tour);
                        log.info("Booking confirmation email sent for booking ID: {} after successful payment",
                                bookingId);
                    } else {
                        log.warn("Tour not found for booking ID: {}", bookingId);
                    }
                } else {
                    log.warn("Booking not found for ID: {}", bookingId);
                }
            }
        } catch (Exception e) {
            log.error("Error sending booking confirmation email for transaction: {}", transaction.getOrderId(), e);
        }
    }
}
