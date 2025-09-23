package com.example.KDBS.service;

import com.example.KDBS.dto.request.BookingRequest;
import com.example.KDBS.dto.response.BookingResponse;
import com.example.KDBS.dto.response.BookingSummaryResponse;
import com.example.KDBS.dto.response.GuestResponse;
import com.example.KDBS.enums.GuestType;
import com.example.KDBS.exception.AppException;
import com.example.KDBS.exception.ErrorCode;
import com.example.KDBS.model.Booking;
import com.example.KDBS.model.BookingGuest;
import com.example.KDBS.model.Tour;
import com.example.KDBS.repository.BookingGuestRepository;
import com.example.KDBS.repository.BookingRepository;
import com.example.KDBS.repository.TourRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingService {

    private final BookingRepository bookingRepository;
    private final BookingGuestRepository bookingGuestRepository;
    private final TourRepository tourRepository;
    private final EmailService emailService;

    @Transactional
    public BookingResponse createBooking(BookingRequest request) {
        // Validate tour exists
        Tour tour = tourRepository.findById(request.getTourId())
                .orElseThrow(() -> new AppException(ErrorCode.TOUR_NOT_FOUND));

        // Validate guest counts match
        validateGuestCounts(request);

        // Create booking first
        Booking booking = Booking.builder()
                .tourId(request.getTourId())
                .contactName(request.getContactName())
                .contactAddress(request.getContactAddress())
                .contactPhone(request.getContactPhone())
                .contactEmail(request.getContactEmail())
                .pickupPoint(request.getPickupPoint())
                .note(request.getNote())
                .departureDate(request.getDepartureDate())
                .adultsCount(request.getAdultsCount())
                .childrenCount(request.getChildrenCount())
                .babiesCount(request.getBabiesCount())
                .build();

        // Save booking first to get ID
        Booking savedBooking = bookingRepository.save(booking);

        // Create guests with booking reference
        List<BookingGuest> guests = request.getGuests().stream()
                .map(guestRequest -> {
                    BookingGuest guest = BookingGuest.builder()
                            .booking(savedBooking) // Set booking reference
                            .fullName(guestRequest.getFullName())
                            .birthDate(guestRequest.getBirthDate())
                            .gender(guestRequest.getGender())
                            .idNumber(guestRequest.getIdNumber())
                            .nationality(guestRequest.getNationality())
                            .guestType(guestRequest.getGuestType())
                            .build();
                    return guest;
                })
                .collect(Collectors.toList());

        // Save guests
        List<BookingGuest> savedGuests = bookingGuestRepository.saveAll(guests);
        
        // Update booking with guests for response
        savedBooking.setGuests(savedGuests);

        return mapToResponse(savedBooking, tour);
    }

    @Transactional(readOnly = true)
    public BookingResponse getBookingById(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));

        Tour tour = tourRepository.findById(booking.getTourId())
                .orElseThrow(() -> new AppException(ErrorCode.TOUR_NOT_FOUND));

        // Load guests
        List<BookingGuest> guests = bookingGuestRepository.findByBookingId(bookingId);
        booking.setGuests(guests);

        return mapToResponse(booking, tour);
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getBookingsByEmail(String email) {
        List<Booking> bookings = bookingRepository.findByContactEmailOrderByCreatedAtDesc(email);
        return bookings.stream()
                .map(booking -> {
                    Tour tour = tourRepository.findById(booking.getTourId()).orElse(null);
                    List<BookingGuest> guests = bookingGuestRepository.findByBookingId(booking.getBookingId());
                    booking.setGuests(guests);
                    return mapToResponse(booking, tour);
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getBookingsByTourId(Long tourId) {
        List<Booking> bookings = bookingRepository.findByTourIdOrderByCreatedAtDesc(tourId);
        Tour tour = tourRepository.findById(tourId).orElse(null);
        
        return bookings.stream()
                .map(booking -> {
                    List<BookingGuest> guests = bookingGuestRepository.findByBookingId(booking.getBookingId());
                    booking.setGuests(guests);
                    return mapToResponse(booking, tour);
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<GuestResponse> getAllGuestsByBookingId(Long bookingId) {
        List<BookingGuest> guests = bookingGuestRepository.findByBookingId(bookingId);

        return guests.stream()
                .map(g -> GuestResponse.builder()
                        .guestId(g.getGuestId())
                        .fullName(g.getFullName())
                        .birthDate(g.getBirthDate())
                        .gender(g.getGender())
                        .idNumber(g.getIdNumber())
                        .nationality(g.getNationality())
                        .guestType(g.getGuestType())
                        .build())
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

    @Transactional(readOnly = true)
    public List<BookingSummaryResponse> getBookingSummaryByEmail(String email) {
        List<Booking> bookings = bookingRepository.findByContactEmailOrderByCreatedAtDesc(email);
        return bookings.stream()
                .map(booking -> {
                    Tour tour = tourRepository.findById(booking.getTourId()).orElse(null);
                    BigDecimal totalAmount = calculateBookingTotal(booking.getBookingId());
                    
                    return BookingSummaryResponse.builder()
                            .bookingId(booking.getBookingId())
                            .tourId(booking.getTourId())
                            .tourName(tour != null ? tour.getTourName() : "Unknown Tour")
                            .contactName(booking.getContactName())
                            .contactPhone(booking.getContactPhone())
                            .contactEmail(booking.getContactEmail())
                            .departureDate(booking.getDepartureDate())
                            .totalGuests(booking.getTotalGuests())
                            .totalAmount(totalAmount)
                            .status("PENDING") // You can add status field to Booking entity
                            .createdAt(booking.getCreatedAt())
                            .build();
                })
                .collect(Collectors.toList());
    }

    private void validateGuestCounts(BookingRequest request) {
        int actualAdults = (int) request.getGuests().stream()
                .filter(g -> g.getGuestType() == GuestType.ADULT)
                .count();
        int actualChildren = (int) request.getGuests().stream()
                .filter(g -> g.getGuestType() == GuestType.CHILD)
                .count();
        int actualBabies = (int) request.getGuests().stream()
                .filter(g -> g.getGuestType() == GuestType.BABY)
                .count();

        if (actualAdults != request.getAdultsCount()) {
            throw new AppException(ErrorCode.INVALID_GUEST_COUNT);
        }
        if (actualChildren != request.getChildrenCount()) {
            throw new AppException(ErrorCode.INVALID_GUEST_COUNT);
        }
        if (actualBabies != request.getBabiesCount()) {
            throw new AppException(ErrorCode.INVALID_GUEST_COUNT);
        }
    }

    private BookingResponse mapToResponse(Booking booking, Tour tour) {
        List<GuestResponse> guestResponses = booking.getGuests().stream()
                .map(guest -> GuestResponse.builder()
                        .guestId(guest.getGuestId())
                        .fullName(guest.getFullName())
                        .birthDate(guest.getBirthDate())
                        .gender(guest.getGender())
                        .idNumber(guest.getIdNumber())
                        .nationality(guest.getNationality())
                        .guestType(guest.getGuestType())
                        .build())
                .collect(Collectors.toList());

        return BookingResponse.builder()
                .bookingId(booking.getBookingId())
                .tourId(booking.getTourId())
                .tourName(tour != null ? tour.getTourName() : "Unknown Tour")
                .contactName(booking.getContactName())
                .contactAddress(booking.getContactAddress())
                .contactPhone(booking.getContactPhone())
                .contactEmail(booking.getContactEmail())
                .pickupPoint(booking.getPickupPoint())
                .note(booking.getNote())
                .departureDate(booking.getDepartureDate())
                .adultsCount(booking.getAdultsCount())
                .childrenCount(booking.getChildrenCount())
                .babiesCount(booking.getBabiesCount())
                .totalGuests(booking.getTotalGuests())
                .createdAt(booking.getCreatedAt())
                .guests(guestResponses)
                .build();
    }

    /**
     * Gửi email xác nhận booking
     */
    @Transactional(readOnly = true)
    public void sendBookingConfirmationEmail(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));

        Tour tour = tourRepository.findById(booking.getTourId())
                .orElseThrow(() -> new AppException(ErrorCode.TOUR_NOT_FOUND));

        emailService.sendBookingConfirmationEmail(booking, tour);
        log.info("Booking confirmation email sent manually for booking ID: {}", bookingId);
    }
}
