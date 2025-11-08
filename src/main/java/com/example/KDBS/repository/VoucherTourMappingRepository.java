package com.example.KDBS.repository;

import com.example.KDBS.model.VoucherTourMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VoucherTourMappingRepository extends JpaRepository<VoucherTourMapping, Long> {
    List<VoucherTourMapping> findByVoucher_VoucherId(Long voucherId);
}


