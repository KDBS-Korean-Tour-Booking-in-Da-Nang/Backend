package com.example.KDBS.repository;

import com.example.KDBS.enums.UserActionTarget;
import com.example.KDBS.enums.UserActionType;
import com.example.KDBS.model.UserActionLog;
import com.example.KDBS.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserActionLogRepository extends JpaRepository<UserActionLog, Long> {
    Page<UserActionLog> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
    List<UserActionLog> findByUserAndActionTypeAndTargetType(
            User user,
            UserActionType actionType,
            UserActionTarget targetType
    );
    List<UserActionLog> findTop30ByUserOrderByCreatedAtDesc(User user);

    List<UserActionLog> findTop30ByOrderByCreatedAtDesc();
    List<UserActionLog> findTop10ByActionTypeAndTargetTypeOrderByCreatedAtDesc(
            UserActionType actionType,
            UserActionTarget targetType
    );


}





