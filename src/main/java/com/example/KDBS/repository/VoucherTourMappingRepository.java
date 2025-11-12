package com.example.KDBS.repository;

import com.example.KDBS.model.VoucherTourMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VoucherTourMappingRepository extends JpaRepository<VoucherTourMapping, Long> {
    List<VoucherTourMapping> findByVoucher_VoucherId(Long voucherId);
    
    @Query("SELECT m.tour.tourId FROM VoucherTourMapping m WHERE m.voucher.voucherId = :voucherId")
    List<Long> findTourIdsByVoucherId(@Param("voucherId") Long voucherId);
}


