package com.example.KDBS.service;

import com.example.KDBS.dto.request.BookingGuestRequest;
import com.example.KDBS.dto.request.BookingRequest;
import com.example.KDBS.dto.response.BookingGuestResponse;
import com.example.KDBS.dto.response.BookingResponse;
import com.example.KDBS.dto.response.BookingSummaryResponse;
import com.example.KDBS.enums.BookingGuestType;
import com.example.KDBS.enums.BookingStatus;
import com.example.KDBS.enums.InsuranceStatus;
import com.example.KDBS.enums.NotificationType;
import com.example.KDBS.exception.AppException;
import com.example.KDBS.exception.ErrorCode;
import com.example.KDBS.mapper.BookingMapper;
import com.example.KDBS.model.*;
import com.example.KDBS.repository.BookingGuestRepository;
import com.example.KDBS.repository.BookingRepository;
import com.example.KDBS.repository.TourRepository;
import com.example.KDBS.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingService {
    private final BookingRepository bookingRepository;
    private final BookingGuestRepository bookingGuestRepository;
    private final TourRepository tourRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final BookingMapper bookingMapper;
    private final NotificationService notificationService;

    @Transactional
    public BookingResponse createBooking(BookingRequest request) {
        Tour tour = tourRepository.findById(request.getTourId())
                .orElseThrow(() -> new AppException(ErrorCode.TOUR_NOT_FOUND));

        validateGuestCounts(request);

        Booking booking = bookingMapper.toBooking(request);
        booking.setTour(tour);
        int totalGuests = (request.getAdultsCount() != null ? request.getAdultsCount() : 1)
                + (request.getChildrenCount() != null ? request.getChildrenCount() : 0)
                + (request.getBabiesCount() != null ? request.getBabiesCount() : 0);
        booking.setTotalGuests(totalGuests);
        Booking savedBooking = bookingRepository.save(booking);

        List<BookingGuest> savedGuests = saveBookingGuests(request.getBookingGuestRequests(), savedBooking);
        savedBooking.setGuests(savedGuests);

        return buildBookingResponse(savedBooking, tour, savedGuests);
    }

    @Transactional
    public BookingResponse updateBooking(long bookingId, BookingRequest request) {
        Booking existingBooking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));

        if (!existingBooking.getTour().getTourId().equals(request.getTourId())) {
            throw new AppException(ErrorCode.TOUR_NOT_FOUND);
        }

        if (existingBooking.getBookingStatus() != BookingStatus.WAITING_FOR_UPDATE) {
            throw new AppException(ErrorCode.BOOKING_CANNOT_BE_UPDATE);
        }

        int totalGuests = (request.getAdultsCount() != null ? request.getAdultsCount() : 1)
                + (request.getChildrenCount() != null ? request.getChildrenCount() : 0)
                + (request.getBabiesCount() != null ? request.getBabiesCount() : 0);
        existingBooking.setTotalGuests(totalGuests);

        validateGuestCounts(request);
        bookingMapper.updateBookingFromRequest(request, existingBooking);

        // Replace guests
        bookingGuestRepository.deleteAll(existingBooking.getGuests());

        List<BookingGuest> savedGuests = saveBookingGuests(request.getBookingGuestRequests(), existingBooking);
        existingBooking.setGuests(savedGuests);

        // Set status to wait for approved after update
        existingBooking.setBookingStatus(BookingStatus.WAITING_FOR_APPROVED);

        return buildBookingResponse(existingBooking, existingBooking.getTour(), savedGuests);
    }

    private List<BookingGuest> saveBookingGuests(List<BookingGuestRequest> guestRequests, Booking booking) {
        List<BookingGuest> guests = guestRequests.stream()
                .map(guestReq -> {
                    BookingGuest guest = bookingMapper.toBookingGuest(guestReq);
                    guest.setBooking(booking);
                    return guest;
                })
                .toList();

        return bookingGuestRepository.saveAll(guests);
    }

    private BookingResponse buildBookingResponse(Booking booking, Tour tour, List<BookingGuest> guests) {
        BookingResponse response = bookingMapper.toBookingResponse(booking);
        response.setGuests(bookingMapper.toBookingGuestResponses(guests));

        // Tạo thông báo cho company (owner của tour)
        createNotificationForBooking(booking, tour);

        return response;
    }

    private void createNotificationForBooking(Booking booking, Tour tour) {
        try {
            Optional<User> companyOpt = userRepository.findById(tour.getCompanyId());
            if (companyOpt.isEmpty()) {
                log.warn("Company not found for tour {} with companyId {}", tour.getTourId(), tour.getCompanyId());
                return;
            }

            User company = companyOpt.get();
            User bookingUser = null;
            if (booking.getUserEmail() != null && !booking.getUserEmail().isEmpty()) {
                bookingUser = userRepository.findByEmail(booking.getUserEmail()).orElse(null);
            }

            String actorName = bookingUser != null && bookingUser.getUsername() != null
                    ? bookingUser.getUsername()
                    : booking.getContactName() != null ? booking.getContactName() : "Người dùng";

            notificationService.createNotification(
                    company.getUserId(),
                    bookingUser != null ? bookingUser.getUserId() : null,
                    NotificationType.NEW_BOOKING,
                    tour.getTourId(),
                    "TOUR",
                    "Có booking mới cho tour của bạn",
                    String.format("%s đã đặt tour \"%s\" của bạn", actorName, tour.getTourName()));

            log.debug("Notification created for new booking: {} on tour {}", booking.getBookingId(), tour.getTourId());
        } catch (Exception e) {
            log.error("Failed to create notification for booking: {}", e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
    public BookingResponse getBookingById(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));

        Tour tour = tourRepository.findById(booking.getTour().getTourId())
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
        List<Booking> bookings = bookingRepository.findByUserEmailOrderByCreatedAtDesc(email);

        return bookings.stream()
                .map(booking -> {
                    Tour tour = tourRepository.findById(booking.getTour().getTourId()).orElse(null);
                    return mapToBookingResponse(booking, tour);
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getBookingsByTourId(Long tourId) {
        List<Booking> bookings = bookingRepository.findByTour_TourIdOrderByCreatedAtDesc(tourId);
        Tour tour = tourRepository.findById(tourId).orElse(null);

        return bookings.stream()
                .map(booking -> mapToBookingResponse(booking, tour))
                .toList();
    }

    private BookingResponse mapToBookingResponse(Booking booking, Tour tour) {
        List<BookingGuest> guests = bookingGuestRepository.findByBooking_BookingId(booking.getBookingId());
        booking.setGuests(guests);

        BookingResponse response = bookingMapper.toBookingResponse(booking);
        response.setTourName(tour != null ? tour.getTourName() : "Unknown Tour");
        response.setGuests(bookingMapper.toBookingGuestResponses(guests));

        return response;
    }

    @Transactional(readOnly = true)
    public List<BookingGuestResponse> getAllGuestsByBookingId(Long bookingId) {
        List<BookingGuest> guests = bookingGuestRepository.findByBooking_BookingId(bookingId);
        return bookingMapper.toBookingGuestResponses(guests);
    }

    @Transactional(readOnly = true)
    public List<BookingSummaryResponse> getBookingSummaryByEmail(String email) {
        List<Booking> bookings = bookingRepository.findByUserEmailOrderByCreatedAtDesc(email);

        return bookings.stream()
                .map(booking -> {
                    Tour tour = tourRepository.findById(booking.getTour().getTourId()).orElse(null);
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

        Tour tour = tourRepository.findById(booking.getTour().getTourId())
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

        Tour tour = tourRepository.findById(booking.getTour().getTourId())
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
            log.info("Processing transaction orderInfo: {}", orderInfo);
            if (orderInfo != null && orderInfo.contains("Booking payment for booking ID:")) {

                // Get booking ID by parsing orderInfo replace and split then get first part and
                // trim
                // Example: "Booking payment for booking ID: 123 | Tour: Amazing Tour - 2 guests
                // on 2023-10-15"
                String bookingIdStr = orderInfo.replace("Booking payment for booking ID:", "")
                        .split("\\|")[0]
                        .trim();
                Long bookingId = Long.parseLong(bookingIdStr);

                log.info("Extracted booking ID: {} from transaction orderInfo", bookingId);
                // Get booking and tour information
                Booking booking = bookingRepository.findByIdWithGuests(bookingId).orElse(null);
                if (booking != null) {
                    Tour tour = tourRepository.findById(booking.getTour().getTourId()).orElse(null);
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

    @Transactional
    public BookingResponse changeBookingStatus(Long bookingId, BookingStatus status) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));
        booking.setBookingStatus(status);

        if (status.equals(BookingStatus.WAITING_FOR_UPDATE)) {
            // TODO send notification for user to update booking
        }

        bookingRepository.save(booking);
        return bookingMapper.toBookingResponse(booking);
    }

    @Transactional
    public BookingGuestResponse changeBookingGuestInsuranceStatus(Long bookingGuestId, InsuranceStatus status) {
        BookingGuest guest = bookingGuestRepository.findById(bookingGuestId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_GUEST_NOT_FOUND));
        guest.setInsuranceStatus(status);
        bookingGuestRepository.save(guest);
        return bookingMapper.toBookingGuestResponse(guest);
    }
}
