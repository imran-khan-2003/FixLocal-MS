package com.fixlocal.controller;

import com.fixlocal.dto.PaymentInitiateResponse;
import com.fixlocal.dto.PaymentVerificationRequest;
import com.fixlocal.entity.Booking;
import com.fixlocal.service.EscrowService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
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
    public ResponseEntity<PaymentInitiateResponse> initiatePayment(
            @PathVariable String bookingId,
            @RequestParam double amount
    ) {
        return ResponseEntity.ok(escrowService.initiatePayment(bookingId, amount));
    }

    @PostMapping({
            "/bookings/{bookingId}/payments/verify",
            "/payments/bookings/{bookingId}/verify"
    })
    public ResponseEntity<Booking> verifyPayment(
            @PathVariable String bookingId,
            @Valid @RequestBody PaymentVerificationRequest request
    ) {
        return ResponseEntity.ok(escrowService.verifyPayment(bookingId, request));
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

    @PostMapping("/payments/webhook")
    public ResponseEntity<Void> handleWebhook(
            @RequestHeader(name = "X-Razorpay-Signature", required = false) String signature,
            @RequestBody String payload
    ) {
        escrowService.handleWebhook(signature, payload);
        return ResponseEntity.ok().build();
    }
}
