package com.example.KDBS.controller;

import com.example.KDBS.dto.request.BanUserRequest;
import com.example.KDBS.dto.request.StaffCreateRequest;
import com.example.KDBS.dto.request.StaffTaskUpdateRequest;
import com.example.KDBS.dto.response.ApiResponse;
import com.example.KDBS.dto.response.UserResponse;
import com.example.KDBS.enums.Role;
import com.example.KDBS.service.StaffService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/staff")
@RequiredArgsConstructor
@CrossOrigin("*")
public class StaffController {

    private final StaffService staffService;

    @PostMapping()
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<UserResponse> createStaff(@RequestBody @Valid StaffCreateRequest request) {
        return ApiResponse.<UserResponse>builder()
                .result(staffService.createStaffAccount(request))
                .build();
    }

    @PutMapping("/staffTask")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<UserResponse> updateStaffTask(@RequestBody @Valid StaffTaskUpdateRequest request) {
        return ApiResponse.<UserResponse>builder()
                .result(staffService.updateStaffTask(request))
                .build();
    }

    @PutMapping("/ban-user/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponse<UserResponse> banOrUnbanUser(@PathVariable("userId") int userId, @RequestBody BanUserRequest request)
     {
        return ApiResponse.<UserResponse>builder()
                .result(staffService.setUserBanStatus(userId, request.isBan(), request.getBanReason()))
                .build();
    }


    //Dùng để up role USER lên COMPANY
    @PutMapping("/update-role/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponse<UserResponse> updateUserRole(@PathVariable int userId, @RequestParam("role") Role newRole) {

        return ApiResponse.<UserResponse>builder()
                .result(staffService.updateUserRole(userId, newRole))
                .build();
    }

    @DeleteMapping("/{staffId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<UserResponse> deleteStaff(@PathVariable int staffId) {
        UserResponse deletedStaff = staffService.deleteStaff(staffId);
        return ApiResponse.<UserResponse>builder()
                .result(deletedStaff)
                .build();
    }

}
