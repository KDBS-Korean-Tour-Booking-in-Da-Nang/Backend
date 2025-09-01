package com.example.KDBS.repository;

import com.example.KDBS.model.BusinessLicense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BusinessLicenseRepository extends JpaRepository<BusinessLicense, Long> {
    @Query("select bl.filePath from BusinessLicense bl where bl.user.email = :email")
    String findFilePathByUserEmail(@Param("email") String email);
}
