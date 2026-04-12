package com.fixlocal.service.impl;

import com.fixlocal.service.NotificationService;
import com.fixlocal.entity.Notification;
import com.fixlocal.enums.NotificationType;
import com.fixlocal.repository.NotificationRepository;
import com.fixlocal.repository.UserRepository;
import com.fixlocal.exception.NotificationException;
import com.fixlocal.exception.ErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    private String getUserIdFromAuthentication(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new NotificationException(ErrorCode.UNAUTHORIZED, ErrorCode.UNAUTHORIZED.getMessage());
        }

        String email = authentication.getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new NotificationException(ErrorCode.USER_NOT_FOUND))
                .getId();
    }

    @Transactional
    public void createNotification(String userId,
                                   String message,
                                   NotificationType type) {

        Notification notification = Notification.builder()
                .userId(userId)
                .message(message)
                .type(type)
                .read(false)
                .createdAt(LocalDateTime.now())
                .build();

        notificationRepository.save(notification);
    }

    public Page<Notification> getUserNotifications(Authentication authentication,
                                                   Pageable pageable) {

        String userId = getUserIdFromAuthentication(authentication);

        return notificationRepository
                .findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    @Transactional
    public void markAsRead(String notificationId,
                           Authentication authentication) {

        String userId = getUserIdFromAuthentication(authentication);

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotificationException(ErrorCode.NOTIFICATION_NOT_FOUND,
                        ErrorCode.NOTIFICATION_NOT_FOUND.getMessage()));

        if (!notification.getUserId().equals(userId)) {
            throw new NotificationException(ErrorCode.NOTIFICATION_ACCESS_FORBIDDEN,
                    ErrorCode.NOTIFICATION_ACCESS_FORBIDDEN.getMessage());
        }

        if (notification.isRead()) {
            throw new NotificationException(ErrorCode.NOTIFICATION_ALREADY_READ,
                    ErrorCode.NOTIFICATION_ALREADY_READ.getMessage());
        }

        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Transactional
    public void markAllAsRead(Authentication authentication) {

        String userId = getUserIdFromAuthentication(authentication);

        List<Notification> unreadNotifications =
                notificationRepository.findByUserIdAndReadFalse(userId);

        for (Notification notification : unreadNotifications) {
            notification.setRead(true);
        }

        notificationRepository.saveAll(unreadNotifications);

        log.info("All notifications marked as read for user {}", userId);
    }
}