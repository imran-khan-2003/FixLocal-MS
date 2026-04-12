package com.fixlocal.service;

import com.fixlocal.exception.PaymentException;
import com.fixlocal.entity.Booking;
import com.fixlocal.enums.BookingStatus;
import com.fixlocal.enums.PaymentStatus;
import com.fixlocal.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

public interface EscrowService {
    public Booking initiatePayment(String bookingId, double amount);
    public Booking authorizePayment(String bookingId);
    public Booking capturePayment(String bookingId);
    public Booking refundPayment(String bookingId);
}
