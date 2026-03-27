package com.fixlocal.controller;

import com.fixlocal.model.NotificationType;
import com.fixlocal.service.NotificationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/test")
    public String test() {
        return "FixLocal Backend Running 🚀";
    }

    @GetMapping("/secure/test")
    public String secureTest() {
        return "Secure endpoint working";
    }

//    @GetMapping("/test-notification")
//    public String testNotification() {
//        notificationService.createNotification(
//                "USER_ID_HERE",
//                "Test notification working 🚀",
//                NotificationType.BOOKING_CREATED
//        );
//
//        return "Notification created";
//    }
}
