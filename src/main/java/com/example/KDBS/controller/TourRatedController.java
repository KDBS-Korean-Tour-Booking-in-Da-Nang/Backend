package com.example.KDBS.controller;

import com.example.KDBS.dto.request.TourRatedRequest;
import com.example.KDBS.dto.response.TourRatedResponse;
import com.example.KDBS.service.TourRatedService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/tourRated")
public class TourRatedController {

    @Autowired
    private TourRatedService tourRatedService;

    /** Create new tourRated */
    @PostMapping
    public ResponseEntity<TourRatedResponse> create(@ModelAttribute TourRatedRequest request) throws IOException {
        return ResponseEntity.ok(tourRatedService.createTourRated(request));
    }

    /** Get all tourRateds */
    @GetMapping
    public ResponseEntity<List<TourRatedResponse>> getAll() {
        return ResponseEntity.ok(tourRatedService.getAllTourRated());
    }

    /** Get tourRated by id */
    @GetMapping("/{id}")
    public ResponseEntity<TourRatedResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(tourRatedService.getTourRatedById(id));
    }

    /** Get tourRateds by tourId */
    @GetMapping("/tour/{tourId}")
    public ResponseEntity<List<TourRatedResponse>> getByTour(@PathVariable Long tourId) {
        return ResponseEntity.ok(tourRatedService.getByTour(tourId));
    }

    /** Update tourRated */
    @PutMapping("/{id}")
    public ResponseEntity<TourRatedResponse> update(@PathVariable Long id,
                                                    @ModelAttribute TourRatedRequest request) throws IOException{
        return ResponseEntity.ok(tourRatedService.updateTourRated(id, request));
    }

    /** Delete tourRated */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        tourRatedService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
