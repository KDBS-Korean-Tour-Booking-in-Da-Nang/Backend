package com.example.KDBS.repository;

import com.example.KDBS.enums.TourStatus;
import com.example.KDBS.model.Tour;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface TourRepository extends JpaRepository<Tour, Long> {
    @Query("SELECT t FROM Tour t LEFT JOIN FETCH t.contents WHERE t.tourId = :id")
    Optional<Tour> findByIdWithContents(Long id);

    @Query("SELECT t FROM Tour t LEFT JOIN FETCH t.contents")
    List<Tour> findAllWithContents();

    @Query("""
    SELECT t 
    FROM Tour t 
    LEFT JOIN FETCH t.contents 
    WHERE t.tourStatus = :status
""")
    List<Tour> findAllPublicTours(@Param("status") TourStatus status);


    // Tìm theo tourName hoặc tourDescription hoặc tourSchedule chứa keyword
    @Query("""
    SELECT t FROM Tour t
    LEFT JOIN t.ratings r
    WHERE
        (:keyword IS NULL OR
            LOWER(CAST(t.tourName AS string)) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(CAST(t.tourDescription AS string)) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR (
                :keyword NOT IN ('đà nẵng', 'da nang')
                AND LOWER(CAST(t.tourSchedule AS string)) LIKE LOWER(CONCAT('%', :keyword, '%'))
            )
        )
        AND (:minPrice IS NULL OR t.adultPrice >= :minPrice)
        AND (:maxPrice IS NULL OR t.adultPrice <= :maxPrice)
    GROUP BY t
    HAVING (:minRating IS NULL OR COALESCE(AVG(r.star), 0) >= :minRating)
""")
    Page<Tour> searchByKeywordAndFilters(
            @Param("keyword") String keyword,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("minRating") Double minRating,
            Pageable pageable
    );


}
