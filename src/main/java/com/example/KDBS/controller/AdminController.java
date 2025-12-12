package com.example.KDBS.controller;

import com.example.KDBS.dto.response.BookingStatisticResponse;
import com.example.KDBS.dto.response.TourStatisticResponse;
import com.example.KDBS.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("api/admin")
@RequiredArgsConstructor
public class AdminController {
    private final AdminService adminService;

    // ADMIN TOUR STATISTIC
    @GetMapping("/tour/statistics")
    public ResponseEntity<TourStatisticResponse> getAdminTourStatistics() {

        return ResponseEntity.ok(
                adminService.getAdminTourStatistics()
        );
    }

    // ADMIN BOOKING STATISTIC
    @GetMapping("/booking/statistics")
    public ResponseEntity<BookingStatisticResponse> getAdminBookingStatistics() {

        return ResponseEntity.ok(
                adminService.getAdminBookingStatistics()
        );
    }


    // ADMIN MONTHLY REVENUE
    @GetMapping("/booking/monthly-revenue")
    public ResponseEntity<Map<String, Object>> getAdminMonthlyRevenue(
            @RequestParam int year
    ) {
        Map<Integer, BigDecimal> monthlyRevenue =
                adminService.getAdminMonthlyRevenue(year);

        return ResponseEntity.ok(
                Map.of(
                        "year", year,
                        "monthlyRevenue", monthlyRevenue
                )
        );
    }

    // COUNT UNBANNED USER
    @GetMapping("/count/unbanned/user")
    public ResponseEntity<Long> countUnbannedUsers() {
        return ResponseEntity.ok(
                adminService.countUnbannedUsers()
        );
    }

    // COUNT UNBANNED COMPANY
    @GetMapping("/count/unbanned/company")
    public ResponseEntity<Long> countUnbannedCompanies() {
        return ResponseEntity.ok(
                adminService.countUnbannedCompanies()
        );
    }

    // COUNT UNBANNED STAFF
    @GetMapping("/count/unbanned/staff")
    public ResponseEntity<Long> countUnbannedStaffs() {
        return ResponseEntity.ok(
                adminService.countUnbannedStaffs()
        );
    }

    @GetMapping("/admin/count/approved")
    public ResponseEntity<Long> countApprovedArticles() {
        return ResponseEntity.ok(
                adminService.countApprovedArticles()
        );
    }





}
