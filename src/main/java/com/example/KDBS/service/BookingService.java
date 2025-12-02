package com.example.KDBS.service;

import com.example.KDBS.dto.request.BookingGuestRequest;
import com.example.KDBS.dto.request.BookingRequest;
import com.example.KDBS.dto.request.ChangeBookingStatusRequest;
import com.example.KDBS.dto.request.CreateComplaintRequest;
import com.example.KDBS.dto.request.ResolveComplaintRequest;
import com.example.KDBS.dto.response.BookingComplaintResponse;
import com.example.KDBS.dto.response.BookingGuestResponse;
import com.example.KDBS.dto.response.BookingResponse;
import com.example.KDBS.dto.response.BookingSummaryResponse;
import com.example.KDBS.dto.response.BookingWithCountResponse;
import com.example.KDBS.enums.*;
import com.example.KDBS.exception.AppException;
import com.example.KDBS.exception.ErrorCode;
import com.example.KDBS.mapper.BookingComplaintMapper;
import com.example.KDBS.mapper.BookingMapper;
import com.example.KDBS.model.*;
import com.example.KDBS.repository.BookingComplaintRepository;
import com.example.KDBS.repository.BookingGuestRepository;
import com.example.KDBS.repository.BookingRepository;
import com.example.KDBS.repository.TourRepository;
import com.example.KDBS.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingService {
    private final BookingRepository bookingRepository;
    private final BookingGuestRepository bookingGuestRepository;
    private final TourRepository tourRepository;
    private final EmailService emailService;
    private final BookingMapper bookingMapper;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final UserActionLogService userActionLogService;
    private final BookingComplaintRepository bookingComplaintRepository;
    private final BookingComplaintMapper bookingComplaintMapper;
    private final StaffService staffService;

    @Transactional
    public BookingResponse createBooking(BookingRequest request) {
        Tour tour = tourRepository.findById(request.getTourId())
                .orElseThrow(() -> new AppException(ErrorCode.TOUR_NOT_FOUND));

        validateGuestCounts(request);

        if (request.getDepartureDate().isBefore(LocalDate.now().plusDays(tour.getTourDeadline() + 1))
                || request.getDepartureDate().isAfter(tour.getTourExpirationDate())) {
            throw new AppException(ErrorCode.DEPARTURE_DATE_INVALID);
        }

        Booking booking = bookingMapper.toBooking(request);
        booking.setTour(tour);
        int totalGuests = (request.getAdultsCount() != null ? request.getAdultsCount() : 1)
                + (request.getChildrenCount() != null ? request.getChildrenCount() : 0)
                + (request.getBabiesCount() != null ? request.getBabiesCount() : 0);
        booking.setTotalGuests(totalGuests);
        booking.setTourEndDate(request.getDepartureDate().plusDays(tour.getTourIntDuration()));
        booking.setAutoFailedDate(LocalDate.now().plusDays(tour.getTourDeadline()));
        Booking savedBooking = bookingRepository.save(booking);

        List<BookingGuest> savedGuests = saveBookingGuests(request.getBookingGuestRequests(), savedBooking);
        savedBooking.setGuests(savedGuests);

        sendNewBookingNotification(savedBooking);
        logBookingCreated(savedBooking);

        return buildBookingResponse(savedBooking, savedGuests);
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

        // Notify company that user has updated booking information
        sendBookingUpdatedByUserNotification(existingBooking);

        return buildBookingResponse(existingBooking, savedGuests);
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

    private BookingResponse buildBookingResponse(Booking booking, List<BookingGuest> guests) {
        BookingResponse response = bookingMapper.toBookingResponse(booking);
        response.setGuests(bookingMapper.toBookingGuestResponses(guests));

        return response;
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
        Tour tour = tourRepository.findById(tourId).orElseThrow(() -> new AppException(ErrorCode.TOUR_NOT_FOUND));

        return bookings.stream()
                .map(booking -> mapToBookingResponse(booking, tour))
                .toList();
    }

    @Transactional(readOnly = true)
    public BookingWithCountResponse getAllBookingsByCompanyId(int companyId) {
        List<Booking> bookings = bookingRepository.findByTour_CompanyIdOrderByCreatedAtDesc(companyId);

        List<BookingResponse> responses = bookings.stream()
                .map(booking -> {
                    Tour tour = booking.getTour();
                    return mapToBookingResponse(booking, tour);
                })
                .toList();

        return new BookingWithCountResponse(responses);
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
        if (guests.isEmpty()) {
            throw new AppException(ErrorCode.BOOKING_NOT_FOUND);
        }
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
    public BookingResponse changeBookingStatus(Long bookingId, ChangeBookingStatusRequest request) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));
        BookingStatus oldStatus = booking.getBookingStatus();
        booking.setBookingStatus(request.getStatus());

        if (request.getMessage() != null && !request.getMessage().isBlank()) {
            booking.setBookingMessage(request.getMessage());
        }

        bookingRepository.save(booking);
        sendBookingStatusNotification(booking, request);
        sendBookingStatusChangeEmails(booking, oldStatus, request.getMessage());
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

    @Transactional
    public void confirmedCompletion(Long bookingId, boolean isCompany) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));
        if (booking.getBookingStatus().equals(BookingStatus.BOOKING_SUCCESS_WAIT_FOR_CONFIRMED)) {
            if (isCompany)
                booking.setCompanyConfirmedCompletion(true);
            else
                booking.setUserConfirmedCompletion(true);
        }

        else
            throw new AppException(ErrorCode.BOOKING_CANNOT_CONFIRM_COMPLETION);

        if (booking.getUserConfirmedCompletion() && booking.getCompanyConfirmedCompletion()) {
            booking.setBookingStatus(BookingStatus.BOOKING_SUCCESS);
            distributeBookingRevenue(booking);
        }
    }

    @Transactional
    public void createBookingComplaint(Long bookingId, CreateComplaintRequest request) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));

        if (!booking.getBookingStatus().equals(BookingStatus.BOOKING_SUCCESS_WAIT_FOR_CONFIRMED)) {
            throw new AppException(ErrorCode.BOOKING_CANNOT_CONFIRM_COMPLETION);
        }

        BookingComplaint complaint = BookingComplaint.builder()
                .booking(booking)
                .message(request.getMessage())
                .build();

        bookingComplaintRepository.save(complaint);
        booking.setBookingStatus(BookingStatus.BOOKING_UNDER_COMPLAINT);
    }

    @Transactional
    public void resolveBookingComplaint(Long complaintId, ResolveComplaintRequest request) {
        staffService.getAuthorizedStaff(StaffTask.FORUM_REPORT_AND_BOOKING_COMPLAINT);

        BookingComplaint complaint = bookingComplaintRepository.findById(complaintId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));

        if (complaint.getResolutionType() != null) {
            return;
        }

        complaint.setResolutionType(request.getResolutionType());
        complaint.setResolvedAt(LocalDate.now().atStartOfDay());

        Booking booking = complaint.getBooking();

        switch (request.getResolutionType()) {
            case USER_FAULT:
                booking.setBookingStatus(BookingStatus.BOOKING_SUCCESS);
                distributeBookingRevenue(booking);
            case NO_FAULT:
                booking.setBookingStatus(BookingStatus.BOOKING_SUCCESS);
                distributeBookingRevenue(booking);
                break;
            case COMPANY_FAULT:
//                booking.setBookingStatus(BookingStatus.BOOKING_UNDER_COMPLAINT); No need since status remain unchanged,
//                implemented for clarity or future changes
                break;
            default:
                break;
        }
    }

    @Transactional(readOnly = true)
    public List<BookingComplaintResponse> getComplaintsByBookingId(Long bookingId) {
        bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));

        List<BookingComplaint> complaints = bookingComplaintRepository.findByBooking_BookingId(bookingId);

        return complaints.stream().map(bookingComplaintMapper::toBookingComplaintResponse).toList();
    }

    @Transactional
    public void checkTourCompletionStatus() {
        List<Booking> bookings = bookingRepository.findByBookingStatusIn(List.of(
                BookingStatus.BOOKING_SUCCESS_WAIT_FOR_CONFIRMED,
                BookingStatus.BOOKING_SUCCESS_PENDING));
        for (Booking booking : bookings) {
            if (booking.getBookingStatus().equals(BookingStatus.BOOKING_SUCCESS_WAIT_FOR_CONFIRMED)) {
                if (LocalDate.now().isAfter(booking.getAutoConfirmedDate())) {
                    booking.setCompanyConfirmedCompletion(true);
                    booking.setUserConfirmedCompletion(true);
                    booking.setBookingStatus(BookingStatus.BOOKING_SUCCESS);
                    distributeBookingRevenue(booking);
                }
            } else if (booking.getBookingStatus().equals(BookingStatus.BOOKING_SUCCESS_PENDING)) {
                if (LocalDate.now().isAfter(booking.getTourEndDate())) {
                    booking.setBookingStatus(BookingStatus.BOOKING_SUCCESS_WAIT_FOR_CONFIRMED);
                }
            }
        }
    }

    @Transactional
    public void checkBookingFailed() {
        List<Booking> bookings = bookingRepository.findByBookingStatusIn(List.of(
                BookingStatus.PENDING_PAYMENT,
                BookingStatus.WAITING_FOR_APPROVED,
                BookingStatus.WAITING_FOR_UPDATE));

        for (Booking booking : bookings) {
            if (LocalDate.now().isAfter(booking.getAutoFailedDate())) {
                booking.setBookingStatus(BookingStatus.BOOKING_FAILED);
            }
        }
    }

    private void sendNewBookingNotification(Booking booking) {
        Integer actorId = resolveUserIdByEmail(booking.getUserEmail());
        String title = "Booking mới - " + booking.getTour().getTourName();
        String message = String.format("Khách %s vừa đặt tour khởi hành %s (%d khách).",
                booking.getContactName(),
                booking.getDepartureDate(),
                booking.getTotalGuests());

        notificationService.pushNotification(
                booking.getTour().getCompanyId(),
                actorId,
                NotificationType.NEW_BOOKING,
                title,
                message,
                booking.getBookingId(),
                "BOOKING");
    }

    private void sendBookingUpdatedByUserNotification(Booking booking) {
        Integer actorId = resolveUserIdByEmail(booking.getUserEmail());
        Integer companyRecipientId = booking.getTour().getCompanyId();

        String title = "Khách hàng đã cập nhật booking";
        String message = String.format("Khách %s đã cập nhật thông tin booking #%d cho tour %s.",
                booking.getContactName(),
                booking.getBookingId(),
                booking.getTour().getTourName());

        notificationService.pushNotification(
                companyRecipientId,
                actorId,
                NotificationType.BOOKING_UPDATED_BY_USER,
                title,
                message,
                booking.getBookingId(),
                "BOOKING");
    }

    private void sendBookingStatusNotification(Booking booking, ChangeBookingStatusRequest request) {
        NotificationType notificationType = mapStatusToNotificationType(request.getStatus());
        if (notificationType == null) {
            return;
        }
        Integer userRecipientId = resolveUserIdByEmail(booking.getUserEmail());
        Integer companyRecipientId = booking.getTour().getCompanyId(); // Company is the actor when changing status

        String defaultMessage;
        if (notificationType == NotificationType.BOOKING_CONFIRMED) {
            defaultMessage = "Booking của bạn đã được xác nhận.";
        } else if (notificationType == NotificationType.BOOKING_REJECTED) {
            defaultMessage = "Booking của bạn đã bị từ chối. Vui lòng liên hệ công ty để biết thêm chi tiết.";
        } else if (notificationType == NotificationType.BOOKING_UPDATE_REQUEST) {
            defaultMessage = "Công ty yêu cầu bạn cập nhật thông tin booking.";
        } else {
            defaultMessage = "";
        }

        String userTitle;
        if (notificationType == NotificationType.BOOKING_CONFIRMED) {
            userTitle = "Booking đã được xác nhận";
        } else if (notificationType == NotificationType.BOOKING_REJECTED) {
            userTitle = "Booking bị từ chối";
        } else if (notificationType == NotificationType.BOOKING_UPDATE_REQUEST) {
            userTitle = "Cần cập nhật booking";
        } else {
            userTitle = "Thông báo booking";
        }

        String message = request.getMessage() != null ? request.getMessage() : defaultMessage;

        // Gửi notification cho user
        notificationService.pushNotification(
                userRecipientId,
                companyRecipientId,
                notificationType,
                userTitle,
                message,
                booking.getBookingId(),
                "BOOKING");

        // Gửi notification cho company về thay đổi trạng thái booking
        String companyTitle;
        if (notificationType == NotificationType.BOOKING_CONFIRMED) {
            companyTitle = "Booking đã được xác nhận";
        } else if (notificationType == NotificationType.BOOKING_REJECTED) {
            companyTitle = "Booking đã bị từ chối";
        } else if (notificationType == NotificationType.BOOKING_UPDATE_REQUEST) {
            companyTitle = "Yêu cầu cập nhật booking";
        } else {
            companyTitle = "Thông báo booking";
        }

        String companyMessage;
        if (notificationType == NotificationType.BOOKING_CONFIRMED) {
            companyMessage = String.format("Booking #%d cho tour %s đã được xác nhận.",
                    booking.getBookingId(), booking.getTour().getTourName());
        } else if (notificationType == NotificationType.BOOKING_REJECTED) {
            companyMessage = String.format("Booking #%d cho tour %s đã bị từ chối.",
                    booking.getBookingId(), booking.getTour().getTourName());
        } else if (notificationType == NotificationType.BOOKING_UPDATE_REQUEST) {
            companyMessage = String.format("Yêu cầu khách hàng cập nhật thông tin booking #%d.",
                    booking.getBookingId());
        } else {
            companyMessage = "";
        }

        notificationService.pushNotification(
                companyRecipientId,
                companyRecipientId,
                notificationType,
                companyTitle,
                companyMessage,
                booking.getBookingId(),
                "BOOKING");
    }

    private NotificationType mapStatusToNotificationType(BookingStatus status) {
        if (status == null) {
            return null;
        }

        if (status == BookingStatus.BOOKING_SUCCESS_PENDING) {
            return NotificationType.BOOKING_CONFIRMED;
        } else if (status == BookingStatus.BOOKING_REJECTED) {
            return NotificationType.BOOKING_REJECTED;
        } else if (status == BookingStatus.WAITING_FOR_UPDATE) {
            return NotificationType.BOOKING_UPDATE_REQUEST;
        } else {
            return null;
        }
    }

    private void logBookingCreated(Booking booking) {
        if (booking == null || booking.getUserEmail() == null || booking.getUserEmail().isBlank()) {
            return;
        }

        userRepository.findByEmail(booking.getUserEmail()).ifPresent(user -> userActionLogService.logAction(
                user,
                UserActionType.CREATE_BOOKING,
                UserActionTarget.BOOKING,
                booking.getBookingId(),
                Map.of(
                        "tourId", booking.getTour().getTourId(),
                        "tourName", booking.getTour().getTourName(),
                        "departureDate",
                        booking.getDepartureDate() != null ? booking.getDepartureDate().toString() : "",
                        "totalGuests", booking.getTotalGuests())));
    }

    private void sendBookingStatusChangeEmails(Booking booking, BookingStatus oldStatus, String message) {
        try {
            if (booking == null) {
                return;
            }

            Tour tour = tourRepository.findById(booking.getTour().getTourId())
                    .orElseThrow(() -> new AppException(ErrorCode.TOUR_NOT_FOUND));
            if (tour == null) {
                return;
            }

            String userEmail = booking.getContactEmail() != null && !booking.getContactEmail().isBlank()
                    ? booking.getContactEmail()
                    : booking.getUserEmail();

            String companyEmail = userRepository.findById(tour.getCompanyId())
                    .map(User::getEmail)
                    .orElse(null);

            if (userEmail != null && !userEmail.isBlank()) {
                emailService.sendBookingStatusUpdateEmail(
                        userEmail,
                        booking,
                        tour,
                        oldStatus,
                        booking.getBookingStatus(),
                        false,
                        message);
            }

            if (companyEmail != null && !companyEmail.isBlank()) {
                emailService.sendBookingStatusUpdateEmail(
                        companyEmail,
                        booking,
                        tour,
                        oldStatus,
                        booking.getBookingStatus(),
                        true,
                        message);
            }
        } catch (Exception e) {
            log.error("Failed to send booking status change emails for booking {}", booking.getBookingId(), e);
        }
    }

    private Integer resolveUserIdByEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        return userRepository.findByEmail(email)
                .map(User::getUserId)
                .orElse(null);
    }

    protected void distributeBookingRevenue(Booking booking) {
        Tour tour = tourRepository.findById(booking.getTour().getTourId())
                .orElseThrow(() -> new AppException(ErrorCode.TOUR_NOT_FOUND));

        BigDecimal grossTotal = calculateBookingTotal(booking.getBookingId());
        BigDecimal discount = booking.getVoucherDiscountApplied() != null
                ? booking.getVoucherDiscountApplied()
                : BigDecimal.ZERO;

        BigDecimal payable = grossTotal.subtract(discount);
        if (payable.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        BigDecimal adminShare = payable.divide(BigDecimal.TEN, 2, RoundingMode.HALF_UP);
        BigDecimal companyShare = payable.subtract(adminShare);

        User companyUser = userRepository.findById(tour.getCompanyId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        User adminUser = userRepository.findFirstByRole(Role.ADMIN)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        companyUser.setBalance(defaultZero(companyUser.getBalance()).add(companyShare));
        adminUser.setBalance(defaultZero(adminUser.getBalance()).add(adminShare));
    }

    private BigDecimal defaultZero(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}