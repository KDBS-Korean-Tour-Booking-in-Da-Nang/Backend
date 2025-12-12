package com.example.KDBS.service;

import com.example.KDBS.dto.response.BookingStatisticResponse;
import com.example.KDBS.dto.response.TourStatisticResponse;
import com.example.KDBS.dto.response.UserStatisticResponse;
import com.example.KDBS.enums.*;
import com.example.KDBS.repository.ArticleRepository;
import com.example.KDBS.repository.BookingRepository;
import com.example.KDBS.repository.TourRepository;
import com.example.KDBS.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final TourRepository tourRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ArticleRepository articleRepository;
    public TourStatisticResponse getAdminTourStatistics() {

        // 1. Tổng tour toàn hệ thống
        long totalTours = tourRepository.countAllTours();

        // 2. Group by status
        List<Object[]> results =
                tourRepository.countAllToursGroupByStatus();

        Map<TourStatus, Long> statusMap =
                new EnumMap<>(TourStatus.class);

        // init tất cả status = 0
        for (TourStatus status : TourStatus.values()) {
            statusMap.put(status, 0L);
        }

        // fill data
        for (Object[] row : results) {
            TourStatus status = (TourStatus) row[0];
            Long count = (Long) row[1];
            statusMap.put(status, count);
        }

        return TourStatisticResponse.builder()
                .totalTours(totalTours)
                .byStatus(statusMap)
                .build();
    }

    // ADMIN BOOKING STATISTIC
    public BookingStatisticResponse getAdminBookingStatistics() {

        // 1. Tổng booking toàn hệ thống
        long totalBookings = bookingRepository.countAllBookings();

        // 2. Group by status
        List<Object[]> results =
                bookingRepository.countAllBookingsGroupByStatus();

        Map<BookingStatus, Long> statusMap =
                new EnumMap<>(BookingStatus.class);

        // init tất cả status = 0
        for (BookingStatus status : BookingStatus.values()) {
            statusMap.put(status, 0L);
        }

        // fill data
        for (Object[] row : results) {
            BookingStatus status = (BookingStatus) row[0];
            Long count = (Long) row[1];
            statusMap.put(status, count);
        }

        return BookingStatisticResponse.builder()
                .totalBookings(totalBookings)
                .byStatus(statusMap)
                .build();
    }

    // ADMIN MONTHLY REVENUE
    public Map<Integer, BigDecimal> getAdminMonthlyRevenue(int year) {

        List<Object[]> results = bookingRepository.getAdminMonthlyRevenue(
                year,
                List.of(
                        BookingStatus.BOOKING_SUCCESS,
                        BookingStatus.BOOKING_BALANCE_SUCCESS
                )
        );

        Map<Integer, BigDecimal> monthlyRevenue = new LinkedHashMap<>();

        // init 12 tháng = 0
        for (int i = 1; i <= 12; i++) {
            monthlyRevenue.put(i, BigDecimal.ZERO);
        }

        // fill data từ DB
        for (Object[] row : results) {
            Integer month = (Integer) row[0];
            BigDecimal revenue = (BigDecimal) row[1];
            monthlyRevenue.put(month, revenue);
        }

        return monthlyRevenue;
    }

    // ADMIN MONTHLY BOOKING COUNT
    public Map<Integer, Long> getAdminMonthlyBookingCount(int year) {

        List<Object[]> results =
                bookingRepository.getAdminMonthlyBookingCount(year);

        Map<Integer, Long> monthlyBookingCount = new LinkedHashMap<>();

        // init 12 tháng = 0
        for (int i = 1; i <= 12; i++) {
            monthlyBookingCount.put(i, 0L);
        }

        // fill data từ DB
        for (Object[] row : results) {
            Integer month = (Integer) row[0];
            Long count = (Long) row[1];
            monthlyBookingCount.put(month, count);
        }

        return monthlyBookingCount;
    }


    // ADMIN USER STATISTIC
    public UserStatisticResponse getAdminUserStatistics() {

        // 1. Tổng user
        long totalUsers = userRepository.countAllUsers();

        // 2. Group by status
        List<Object[]> results = userRepository.countAllUsersGroupByStatus();

        Map<Status, Long> statusMap = new EnumMap<>(Status.class);

        // init tất cả status = 0
        for (Status status : Status.values()) {
            statusMap.put(status, 0L);
        }

        // fill data
        for (Object[] row : results) {
            Status status = (Status) row[0];
            Long count = (Long) row[1];
            statusMap.put(status, count);
        }

        return UserStatisticResponse.builder()
                .totalUsers(totalUsers)
                .byStatus(statusMap)
                .build();
    }

    // COUNT UNBANNED USER ROLE = USER
    public long countUnbannedUsers() {
        return userRepository.countUsersByStatusAndRole(
                Status.UNBANNED,
                Role.USER
        );
    }

    // COUNT UNBANNED USER ROLE = COMPANY
    public long countUnbannedCompanies() {
        return userRepository.countUsersByStatusAndRole(
                Status.UNBANNED,
                Role.COMPANY
        );
    }

    // COUNT UNBANNED USER ROLE = STAFF
    public long countUnbannedStaffs() {
        return userRepository.countUsersByStatusAndRole(
                Status.UNBANNED,
                Role.STAFF
        );
    }

    public long countApprovedArticles() {
        return articleRepository.countArticlesByStatus(ArticleStatus.APPROVED);
    }



}
