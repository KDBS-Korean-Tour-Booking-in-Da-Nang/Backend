package com.example.KDBS.controller;

import com.example.KDBS.dto.request.TourRequest;
import com.example.KDBS.dto.response.TourPreviewResponse;
import com.example.KDBS.dto.response.TourResponse;
import com.example.KDBS.service.TourService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/tour")
public class TourController {

    @Autowired
    private TourService tourService;

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

    /** Read one tour by id */
    @GetMapping("/{id}")
    public ResponseEntity<TourResponse> getTour(@PathVariable Long id) {
        return ResponseEntity.ok(tourService.getTourById(id));
    }

    /** Get tours for preview /api/tour/preview/{id}*/
    @GetMapping("/preview/{id}")
    public ResponseEntity<TourPreviewResponse> getPreviewTourById(@PathVariable Long id) {
        return ResponseEntity.ok(tourService.getTourPreviewById(id));
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



    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('COMPANY')")
    public ResponseEntity<TourResponse> updateTour(
            @PathVariable Long id,
            @RequestPart("data") @Valid TourRequest request,
            @RequestPart(value = "tourImg", required = false) MultipartFile tourImg
    ) throws IOException {
        return ResponseEntity.ok(tourService.updateTour(id, request, tourImg));
    }

    /** Delete a tour */
    @DeleteMapping("/{id}")
    @PreAuthorize("tourSecurity.canDeleteTour(#id, #userEmail)")
    public ResponseEntity<Void> deleteTour(@PathVariable Long id,@RequestParam String userEmail) {
        tourService.deleteTour(id);
        return ResponseEntity.noContent().build();
    }
}
