package com.fixlocal.controller;

import com.fixlocal.dto.AdminStatsDTO;
import com.fixlocal.model.User;
import com.fixlocal.model.Booking;
import com.fixlocal.service.AdminService;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/users")
    public ResponseEntity<Page<User>> getUsers(
            Pageable pageable,
            @RequestParam(value = "search", required = false) String search
    ) {
        return ResponseEntity.ok(adminService.getAllUsers(pageable, search));
    }

    @GetMapping({"/tradespersons", "/trades"})
    public ResponseEntity<Page<User>> getTradespersons(
            Pageable pageable,
            @RequestParam(value = "search", required = false) String search
    ) {
        return ResponseEntity.ok(adminService.getAllTradespersons(pageable, search));
    }

    @PatchMapping("/users/{id}/block")
    public ResponseEntity<Void> blockUser(@PathVariable String id) {
        adminService.blockUser(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/users/{id}/unblock")
    public ResponseEntity<Void> unblockUser(@PathVariable String id) {
        adminService.unblockUser(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/tradespersons/{id}/verify")
    public ResponseEntity<Void> verifyTradesperson(@PathVariable String id) {
        adminService.verifyTradesperson(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/bookings")
    public ResponseEntity<Page<Booking>> getAllBookings(Pageable pageable) {
        return ResponseEntity.ok(adminService.getAllBookings(pageable));
    }

    @GetMapping("/stats")
    public ResponseEntity<AdminStatsDTO> getStats() {

        return ResponseEntity.ok(
                adminService.getAdminStats()
        );
    }
}