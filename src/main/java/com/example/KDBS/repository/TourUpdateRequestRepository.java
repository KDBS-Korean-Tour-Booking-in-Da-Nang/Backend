package com.example.KDBS.repository;

import com.example.KDBS.enums.TourUpdateStatus;
import com.example.KDBS.model.TourUpdateRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TourUpdateRequestRepository extends JpaRepository<TourUpdateRequest, Long> {

    List<TourUpdateRequest> findByStatus(TourUpdateStatus status);

    List<TourUpdateRequest> findByOriginalTour_TourId(Long tourId);
}
