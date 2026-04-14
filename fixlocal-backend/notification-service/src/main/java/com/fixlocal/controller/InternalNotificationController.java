package com.fixlocal.controller;

import com.fixlocal.enums.NotificationType;
import com.fixlocal.service.NotificationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/notifications")
@RequiredArgsConstructor
public class InternalNotificationController {

    private final NotificationService notificationService;

    @PostMapping
    public ResponseEntity<Void> createNotification(@Valid @RequestBody InternalNotificationRequest request) {
        notificationService.createNotification(request.getUserId(), request.getMessage(), request.getType());
        return ResponseEntity.ok().build();
    }

    @Data
    public static class InternalNotificationRequest {
        @NotBlank
        private String userId;

        @NotBlank
        private String message;

        @NotNull
        private NotificationType type;
    }
}
