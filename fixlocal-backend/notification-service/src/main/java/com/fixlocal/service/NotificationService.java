package com.fixlocal.service;

import com.fixlocal.entity.Notification;
import com.fixlocal.enums.NotificationType;
import com.fixlocal.repository.NotificationRepository;
import com.fixlocal.repository.UserRepository;
import com.fixlocal.exception.ResourceNotFoundException;
import com.fixlocal.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

public interface NotificationService {
    public void createNotification(String userId, String message, NotificationType type);
    public Page<Notification> getUserNotifications(Authentication authentication, Pageable pageable);
    public void markAsRead(String notificationId, Authentication authentication);
    public void markAllAsRead(Authentication authentication);
}
