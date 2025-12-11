package com.example.KDBS.repository;

import com.example.KDBS.model.SuggestedTour;
import com.example.KDBS.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SuggestedTourRepository extends JpaRepository<SuggestedTour, Long> {
    List<SuggestedTour> findByUser(User user);

    void deleteByUser(User user);
}
