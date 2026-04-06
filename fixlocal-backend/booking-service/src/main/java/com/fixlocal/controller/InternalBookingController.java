package com.fixlocal.controller;

import com.fixlocal.dto.InternalBookingDTO;
import com.fixlocal.dto.InternalAdminBookingStatsDTO;
import com.fixlocal.dto.InternalReviewUpdateRequest;
import com.fixlocal.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/internal/bookings")
@RequiredArgsConstructor
public class InternalBookingController {

    private final BookingService bookingService;

    @GetMapping("/{bookingId}")
    public InternalBookingDTO getBookingById(@PathVariable String bookingId) {
        return bookingService.getInternalBookingById(bookingId);
    }

    @PutMapping("/{bookingId}/review")
    public void applyReview(
            @PathVariable String bookingId,
            @RequestBody InternalReviewUpdateRequest request
    ) {
        bookingService.applyReviewUpdate(bookingId, request);
    }

    @GetMapping("/admin/bookings")
    public Page<InternalBookingDTO> getAdminBookings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return bookingService.getAdminBookings(pageable);
    }

    @GetMapping("/admin/stats")
    public InternalAdminBookingStatsDTO getAdminStats() {
        return bookingService.getAdminBookingStats();
    }

    @GetMapping("/stats/user/{userId}")
    public Map<String, Long> getUserStats(@PathVariable String userId) {
        return bookingService.getUserStatusCounts(userId);
    }

    @GetMapping("/stats/tradesperson/{tradespersonId}")
    public Map<String, Long> getTradespersonStats(@PathVariable String tradespersonId) {
        return bookingService.getTradespersonStatusCounts(tradespersonId);
    }
}
