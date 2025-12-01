package com.example.KDBS.repository;

import com.example.KDBS.model.Voucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

@Repository
public interface VoucherRepository extends JpaRepository<Voucher, Long> {

    Optional<Voucher> findByCompanyIdAndCode(Integer companyId, String code);

    List<Voucher> findByCompanyId(Integer companyId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT v FROM Voucher v WHERE v.voucherId = :id")
    Optional<Voucher> findByIdForUpdate(@Param("id") Long id);

    @Query(""" 
            SELECT v FROM Voucher v
            JOIN Tour t ON t.tourId = :tourId
            LEFT JOIN VoucherTourMapping m ON m.voucher.voucherId = v.voucherId
            WHERE m.tour.tourId = :tourId
            OR (
                    v.companyId = t.companyId
                    AND m IS NULL
                )
        """)
    List<Voucher> findAllVoucherByTourId(Long tourId);
}


