package com.example.KDBS.repository;

import com.example.KDBS.model.UserActionLog;
import com.example.KDBS.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserActionLogRepository extends JpaRepository<UserActionLog, Long> {
    Page<UserActionLog> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
}





