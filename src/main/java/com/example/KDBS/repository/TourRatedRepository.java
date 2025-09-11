package com.example.KDBS.repository;

import com.example.KDBS.model.TourRated;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TourRatedRepository extends JpaRepository<TourRated, Long> {
}
