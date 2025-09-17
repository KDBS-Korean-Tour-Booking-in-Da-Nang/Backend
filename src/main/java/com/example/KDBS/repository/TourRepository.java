package com.example.KDBS.repository;

import com.example.KDBS.model.Tour;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TourRepository extends JpaRepository<Tour, Long> {
    @Query("SELECT t FROM Tour t LEFT JOIN FETCH t.contents c WHERE t.tourId = :id")
    Optional<Tour> findByIdWithContents(Long id);

    @Query("SELECT t FROM Tour t")
    List<Tour> findAllWithContents();
}
