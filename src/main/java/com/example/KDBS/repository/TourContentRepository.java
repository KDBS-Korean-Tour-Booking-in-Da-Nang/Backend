package com.example.KDBS.repository;

import com.example.KDBS.model.TourContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TourContentRepository extends JpaRepository<TourContent, Long> {
}
