package com.fixlocal.controller;

import com.fixlocal.model.Booking;
import com.fixlocal.service.EscrowService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class PaymentController {

    private final EscrowService escrowService;

    @PostMapping({
            "/bookings/{bookingId}/payments/initiate",
            "/payments/bookings/{bookingId}/initiate"
    })
    public ResponseEntity<Booking> initiatePayment(
            @PathVariable String bookingId,
            @RequestParam double amount
    ) {
        return ResponseEntity.ok(escrowService.initiatePayment(bookingId, amount));
    }

    @PostMapping({
            "/bookings/{bookingId}/payments/authorize",
            "/payments/bookings/{bookingId}/authorize"
    })
    public ResponseEntity<Booking> authorizePayment(@PathVariable String bookingId) {
        return ResponseEntity.ok(escrowService.authorizePayment(bookingId));
    }

    @PostMapping({
            "/bookings/{bookingId}/payments/capture",
            "/payments/bookings/{bookingId}/capture"
    })
    public ResponseEntity<Booking> capturePayment(@PathVariable String bookingId) {
        return ResponseEntity.ok(escrowService.capturePayment(bookingId));
    }

    @PostMapping({
            "/bookings/{bookingId}/payments/refund",
            "/payments/bookings/{bookingId}/refund"
    })
    public ResponseEntity<Booking> refundPayment(@PathVariable String bookingId) {
        return ResponseEntity.ok(escrowService.refundPayment(bookingId));
    }
}
