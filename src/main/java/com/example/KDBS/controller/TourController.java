package com.example.KDBS.controller;

import com.example.KDBS.dto.request.TourDeleteCreateRequest;
import com.example.KDBS.dto.request.TourRequest;
import com.example.KDBS.dto.request.TourUpdateCreateRequest;
import com.example.KDBS.dto.response.*;
import com.example.KDBS.enums.TourStatus;
import com.example.KDBS.service.TourDeleteService;
import com.example.KDBS.service.TourService;
import com.example.KDBS.service.TourUpdateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/tour")
@RequiredArgsConstructor
public class TourController {
    private final TourService tourService;
    private final TourUpdateService tourUpdateService;
    private final TourDeleteService tourDeleteService;

    /** TinyMCE inline image upload endpoint */
    @PostMapping(value = "/content-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadEditorImage(@RequestParam("file") MultipartFile file) throws IOException {
        return ResponseEntity.ok(tourService.saveEditorImage(file));
    }

    /** Create a tour with REQUIRED main image */
    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('COMPANY')")
    public ResponseEntity<TourResponse> createTour(
            @RequestPart("data") @Valid TourRequest request,
            @RequestPart("tourImage") MultipartFile tourImg
    ) throws IOException {
        return ResponseEntity.ok(tourService.createTour(request, tourImg));
    }

    /** Read all tours */
    @GetMapping
    public ResponseEntity<List<TourResponse>> getAllTours() {
        return ResponseEntity.ok(tourService.getAllTours());
    }

    @GetMapping("/company/{companyId}")
    public ResponseEntity<List<TourResponse>> getToursByCompanyId(@PathVariable int companyId) {
        return ResponseEntity.ok(tourService.getAllToursByCompanyId(companyId));
    }

    @GetMapping("/public")
    public ResponseEntity<List<TourResponse>> getAllPublicTours() {
        return ResponseEntity.ok(tourService.getAllPublicTours());
    }

    /** Read one tour by id */
    @GetMapping("/{tourId}")
    public ResponseEntity<TourResponse> getTour(@PathVariable Long tourId) {
        return ResponseEntity.ok(tourService.getTourById(tourId));
    }

    /** Get tours for preview /api/tour/preview/{id}*/
    @GetMapping("/preview/{tourId}")
    public ResponseEntity<TourPreviewResponse> getPreviewTourById(@PathVariable Long tourId) {
        return ResponseEntity.ok(tourService.getTourPreviewById(tourId));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<TourResponse>> searchTours(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Double minRating,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(tourService.searchToursWithFilters(keyword, minPrice, maxPrice, minRating, pageable));
    }

    //COMPANY UPDATE TOUR REQUEST
    //COMPANY
    @PostMapping(value = "/{tourId}/update-request", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('COMPANY')")
    public ResponseEntity<TourUpdateRequestResponse> createUpdateRequest(
            @PathVariable Long tourId,
            @RequestPart("data") @Valid TourUpdateCreateRequest request,
            @RequestPart(value = "tourImg", required = false) MultipartFile tourImg
    ) throws Exception {
        return ResponseEntity.ok(tourUpdateService.createUpdateRequest(tourId, request, tourImg));
    }

    //STAFF CHECK UPDATE TOUR REQUEST
    @GetMapping("/update-requests/pending")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<List<TourUpdateRequestResponse>> pendingRequests() {
        return ResponseEntity.ok(tourUpdateService.getPendingRequests());
    }

    @PutMapping("/update-request/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<TourResponse> approve(
            @PathVariable Long id,
            @RequestParam(required = false) String note) throws Exception {
        return ResponseEntity.ok(tourUpdateService.approveRequest(id, note));
    }

    @PutMapping("/update-request/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<Void> reject(
            @PathVariable Long id,
            @RequestParam(required = false) String note) {
        tourUpdateService.rejectRequest(id, note);
        return ResponseEntity.noContent().build();
    }

    //COMPANY DELETE TOUR REQUEST
    //COMPANY
    @PostMapping("/{tourId}/delete-request")
    @PreAuthorize("hasRole('COMPANY')")
    public ResponseEntity<TourDeleteRequestResponse> createDeleteRequest(
            @PathVariable Long tourId,
            @RequestBody(required = false) TourDeleteCreateRequest request
    ) {
        String note = request != null ? request.getNote() : null;
        return ResponseEntity.ok(tourDeleteService.createDeleteRequest(tourId, note));
    }


    //STAFF CHECK DELETE TOUR REQUEST
    @GetMapping("/delete-requests/pending")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<List<TourDeleteRequestResponse>> getDeletePending() {
        return ResponseEntity.ok(tourDeleteService.getPendingRequests());
    }

    @PutMapping("/delete-request/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<Void> approveDelete(@PathVariable Long id,
                                              @RequestParam(required = false) String note) {
        tourDeleteService.approve(id, note);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/delete-request/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<Void> rejectDelete(@PathVariable Long id,
                                             @RequestParam(required = false) String note) {
        tourDeleteService.reject(id, note);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/change-status/{tourId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<TourResponse> changeTourStatus(@PathVariable Long tourId, @RequestParam TourStatus status) {
        return ResponseEntity.ok(tourService.changeTourStatus(tourId, status));
    }

    //COMPANY TOUR STATISTIC
    @GetMapping("/company/{companyId}/statistics")
    public ResponseEntity<TourStatisticResponse> getCompanyTourStatistics(@PathVariable int companyId) {

        return ResponseEntity.ok(
                tourService.getCompanyTourStatistics(companyId)
        );
    }

}
