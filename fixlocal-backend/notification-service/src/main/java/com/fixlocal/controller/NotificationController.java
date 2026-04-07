package com.fixlocal.controller;

import com.fixlocal.entity.Notification;
import com.fixlocal.service.NotificationService;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<Page<Notification>> getMyNotifications(
            Authentication authentication,
            Pageable pageable
    ) {
        return ResponseEntity.ok(
                notificationService.getUserNotifications(authentication, pageable)
        );
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<String> markAsRead(
            @PathVariable String id,
            Authentication authentication
    ) {
        notificationService.markAsRead(id, authentication);
        return ResponseEntity.ok("Notification marked as read");
    }

    @PutMapping("/read-all")
    public ResponseEntity<String> markAllAsRead(Authentication authentication) {
        notificationService.markAllAsRead(authentication);
        return ResponseEntity.ok("All notifications marked as read");
    }
}