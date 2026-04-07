package com.fixlocal.repository;

import com.fixlocal.entity.Notification;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface NotificationRepository extends MongoRepository<Notification, String> {

    Page<Notification> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);

    boolean existsByUserIdAndReadFalse(String userId);

    List<Notification> findByUserIdAndReadFalse(String userId);

    void deleteByUserId(String userId);
}