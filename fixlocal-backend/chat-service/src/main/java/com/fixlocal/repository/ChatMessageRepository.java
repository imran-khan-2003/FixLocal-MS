package com.fixlocal.repository;

import com.fixlocal.entity.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ChatMessageRepository extends MongoRepository<ChatMessage, String> {

    Page<ChatMessage> findByConversationIdOrderByCreatedAtDesc(String conversationId, Pageable pageable);
}
