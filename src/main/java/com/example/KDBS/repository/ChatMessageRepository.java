package com.example.KDBS.repository;

import com.example.KDBS.model.ChatMessage;
import com.example.KDBS.model.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends CrudRepository<ChatMessage, Long> {
    @Query("SELECT m FROM ChatMessage m " +
            "WHERE (m.sender = :user1 AND m.receiver = :user2) " +
            "   OR (m.sender = :user2 AND m.receiver = :user1) " +
            "ORDER BY m.timestamp ASC")
    List<ChatMessage> findConversationBetween(@Param("user1") User user1,
                                              @Param("user2") User user2);

    List<ChatMessage> findBySenderOrReceiverOrderByTimestampDesc(User sender, User receiver);
}
