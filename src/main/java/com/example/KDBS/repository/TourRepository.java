package com.example.KDBS.repository;

import com.example.KDBS.model.Tour;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TourRepository extends JpaRepository<Tour, Long> {
    @Query("SELECT t FROM Tour t LEFT JOIN FETCH t.contents WHERE t.tourId = :id")
    Optional<Tour> findByIdWithContents(Long id);

    @Query("SELECT t FROM Tour t LEFT JOIN FETCH t.contents")
    List<Tour> findAllWithContents();

    // Tìm theo tourName hoặc tourDescription hoặc tourSchedule chứa keyword
    @Query("""
    SELECT t FROM Tour t
    WHERE 
        LOWER(CAST(t.tourName AS string)) LIKE LOWER(CONCAT('%', :keyword, '%'))
        OR LOWER(CAST(t.tourDescription AS string)) LIKE LOWER(CONCAT('%', :keyword, '%'))
        OR (
            :keyword NOT IN ('đà nẵng', 'da nang')
            AND LOWER(CAST(t.tourSchedule AS string)) LIKE LOWER(CONCAT('%', :keyword, '%'))
        )
""")
    Page<Tour> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

}
