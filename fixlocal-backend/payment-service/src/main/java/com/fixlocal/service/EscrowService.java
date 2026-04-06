package com.fixlocal.service;

import com.fixlocal.exception.BadRequestException;
import com.fixlocal.exception.ResourceNotFoundException;
import com.fixlocal.model.Booking;
import com.fixlocal.model.BookingStatus;
import com.fixlocal.model.PaymentStatus;
import com.fixlocal.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EscrowService {

    private final BookingRepository bookingRepository;

    @Transactional
    public Booking initiatePayment(String bookingId, double amount) {

        Booking booking = getBooking(bookingId);

        if (booking.getPaymentStatus() != null) {

            if (booking.getPaymentStatus() == PaymentStatus.REFUNDED
                    || booking.getPaymentStatus() == PaymentStatus.FAILED) {

                booking.setPaymentStatus(PaymentStatus.INITIATED);
                booking.setPaymentIntentId("pi_" + UUID.randomUUID());
                booking.setPrice(amount);
                return bookingRepository.save(booking);
            }

            if (booking.getPaymentStatus() == PaymentStatus.CAPTURED) {
                throw new BadRequestException("Payment already captured");
            }

            log.info("Payment already in progress for booking {}", bookingId);
            return booking;
        }

        booking.setPaymentStatus(PaymentStatus.INITIATED);
        booking.setPaymentIntentId("pi_" + UUID.randomUUID());
        booking.setPrice(amount);

        return bookingRepository.save(booking);
    }

    @Transactional
    public Booking authorizePayment(String bookingId) {

        Booking booking = getBooking(bookingId);
        ensureIntentExists(booking);

        booking.setPaymentStatus(PaymentStatus.AUTHORIZED);
        return bookingRepository.save(booking);
    }

    @Transactional
    public Booking capturePayment(String bookingId) {

        Booking booking = getBooking(bookingId);
        ensureIntentExists(booking);

        if (booking.getStatus() != BookingStatus.COMPLETED) {
            throw new BadRequestException("Payment capture allowed only on completed bookings");
        }

        booking.setPaymentStatus(PaymentStatus.CAPTURED);
        return bookingRepository.save(booking);
    }

    @Transactional
    public Booking refundPayment(String bookingId) {

        Booking booking = getBooking(bookingId);
        ensureIntentExists(booking);

        booking.setPaymentStatus(PaymentStatus.REFUNDED);
        return bookingRepository.save(booking);
    }

    private Booking getBooking(String bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
    }

    private void ensureIntentExists(Booking booking) {
        if (booking.getPaymentIntentId() == null) {
            throw new BadRequestException("Payment intent missing");
        }
    }
}
