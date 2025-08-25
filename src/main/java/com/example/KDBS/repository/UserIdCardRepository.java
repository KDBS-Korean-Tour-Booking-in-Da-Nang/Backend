package com.example.KDBS.repository;

import com.example.KDBS.model.UserIdCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserIdCardRepository extends JpaRepository<UserIdCard, Long> {
    @Query("select u.frontImagePath from UserIdCard u where u.user.email = :email")
    String findFrontImagePathByUserEmail(@Param("email") String email);

    @Query("select u.backImagePath from UserIdCard u where u.user.email = :email")
    String findBackImagePathByUserEmail(@Param("email") String email);
}
