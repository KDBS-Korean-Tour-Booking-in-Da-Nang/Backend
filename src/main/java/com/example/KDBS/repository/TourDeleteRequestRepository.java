package com.example.KDBS.repository;

import com.example.KDBS.enums.TourDeleteStatus;
import com.example.KDBS.model.TourDeleteRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TourDeleteRequestRepository extends JpaRepository<TourDeleteRequest, Long> {

    List<TourDeleteRequest> findByStatus(TourDeleteStatus status);
}
