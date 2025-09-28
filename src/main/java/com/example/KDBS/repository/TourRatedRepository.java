package com.example.KDBS.repository;

import com.example.KDBS.model.TourRated;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TourRatedRepository extends JpaRepository<TourRated, Long> {
    List<TourRated> findByTour_TourId(Long tourId);

    Optional<TourRated> findByTour_TourIdAndUser_UserId(Long tourId, int userId);
}
