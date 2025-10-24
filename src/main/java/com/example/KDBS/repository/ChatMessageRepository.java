package com.example.KDBS.repository;

import com.example.KDBS.model.ChatMessage;
import com.example.KDBS.model.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends CrudRepository<ChatMessage, Long> {
    List<ChatMessage> findBySenderAndReceiverOrReceiverAndSenderOrderByTimestampAsc(
            User sender1, User receiver1,
            User receiver2, User sender2);

    List<ChatMessage> findBySenderOrReceiverOrderByTimestampDesc(User sender, User receiver);
}
