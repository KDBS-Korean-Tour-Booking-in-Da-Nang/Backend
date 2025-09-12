package com.example.KDBS.repository;

import com.example.KDBS.model.TourContentImg;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TourContentImgRepository extends JpaRepository<TourContentImg, Long> {
}
