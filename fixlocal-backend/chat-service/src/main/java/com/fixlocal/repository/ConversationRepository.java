package com.fixlocal.repository;

import com.fixlocal.entity.Conversation;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface ConversationRepository extends MongoRepository<Conversation, String> {

    Optional<Conversation> findByBookingId(String bookingId);
}
