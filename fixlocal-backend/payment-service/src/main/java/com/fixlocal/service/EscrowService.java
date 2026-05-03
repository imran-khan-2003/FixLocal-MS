package com.fixlocal.service;

import com.fixlocal.dto.PaymentInitiateResponse;
import com.fixlocal.dto.PaymentVerificationRequest;
import com.fixlocal.entity.Booking;

public interface EscrowService {
    PaymentInitiateResponse initiatePayment(String bookingId, double amount);

    Booking authorizePayment(String bookingId);

    Booking capturePayment(String bookingId);

    Booking refundPayment(String bookingId);

    Booking verifyPayment(String bookingId, PaymentVerificationRequest request);

    void handleWebhook(String signature, String payload);
}
