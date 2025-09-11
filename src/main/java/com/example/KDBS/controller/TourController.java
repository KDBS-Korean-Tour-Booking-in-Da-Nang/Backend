package com.example.KDBS.controller;

import com.example.KDBS.dto.request.TourRequest;
import com.example.KDBS.dto.response.TourResponse;
import com.example.KDBS.service.TourService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/tour")
public class TourController {

    private final TourService tourService;

    public TourController(TourService tourService) {
        this.tourService = tourService;
    }

    /** TinyMCE inline image upload endpoint */
    @PostMapping(value = "/content-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadEditorImage(@RequestParam("file") MultipartFile file) throws IOException {
        return ResponseEntity.ok(tourService.saveEditorImage(file));
    }

    /** Create a tour with REQUIRED main image */
    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
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

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<TourResponse> updateTour(
            @PathVariable Long id,
            @RequestPart("data") @Valid TourRequest request,
            @RequestPart(value = "tourImg", required = false) MultipartFile tourImg
    ) throws IOException {
        return ResponseEntity.ok(tourService.updateTour(id, request, tourImg));
    }

    /** Delete a tour */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTour(@PathVariable Long id) {
        tourService.deleteTour(id);
        return ResponseEntity.noContent().build();
    }
}
