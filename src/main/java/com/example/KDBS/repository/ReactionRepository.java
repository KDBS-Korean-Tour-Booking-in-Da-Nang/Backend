package com.example.KDBS.repository;

import com.example.KDBS.enums.ReactionTargetType;
import com.example.KDBS.enums.ReactionType;
import com.example.KDBS.model.Reaction;
import com.example.KDBS.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReactionRepository extends JpaRepository<Reaction, Long> {
    
    Optional<Reaction> findByUserAndTargetIdAndTargetType(User user, Long targetId, ReactionTargetType targetType);

    List<Reaction> findByTargetIdAndTargetType(Long targetId, ReactionTargetType targetType);
    
    @Query("SELECT r.reactionType, COUNT(r) FROM Reaction r WHERE r.targetId = :targetId AND r.targetType = :targetType GROUP BY r.reactionType")
    List<Object[]> countReactionsByType(@Param("targetId") Long targetId, @Param("targetType") ReactionTargetType targetType);

    void deleteByUserAndTargetIdAndTargetType(User user, Long targetId, ReactionTargetType targetType);
}
