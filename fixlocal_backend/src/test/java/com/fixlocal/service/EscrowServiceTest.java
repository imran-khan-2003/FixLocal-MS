package com.fixlocal.service;

import com.fixlocal.exception.BadRequestException;
import com.fixlocal.exception.ResourceNotFoundException;
import com.fixlocal.model.Booking;
import com.fixlocal.model.BookingStatus;
import com.fixlocal.model.PaymentStatus;
import com.fixlocal.repository.BookingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EscrowServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private EscrowService escrowService;

    private Booking booking;

    @BeforeEach
    void setup() {
        booking = Booking.builder()
                .id("booking-1")
                .status(BookingStatus.ACCEPTED)
                .build();
    }

    @Test
    void initiatePayment_setsIntentAndStatus() {

        when(bookingRepository.findById("booking-1"))
                .thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Booking updated = escrowService.initiatePayment("booking-1", 150.0);

        assertThat(updated.getPaymentStatus()).isEqualTo(PaymentStatus.INITIATED);
        assertThat(updated.getPaymentIntentId()).isNotNull();
        assertThat(updated.getPrice()).isEqualTo(150.0);
    }

    @Test
    void initiatePayment_allowsRetryAfterRefund() {

        booking.setPaymentStatus(PaymentStatus.REFUNDED);
        booking.setPaymentIntentId("pi_old");

        when(bookingRepository.findById("booking-1"))
                .thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Booking retried = escrowService.initiatePayment("booking-1", 200.0);

        assertThat(retried.getPaymentStatus()).isEqualTo(PaymentStatus.INITIATED);
        assertThat(retried.getPaymentIntentId()).isNotEqualTo("pi_old");
        assertThat(retried.getPrice()).isEqualTo(200.0);
    }

    @Test
    void capturePayment_requiresCompletedBooking() {

        booking.setPaymentStatus(PaymentStatus.AUTHORIZED);
        booking.setPaymentIntentId("pi_123");
        booking.setStatus(BookingStatus.COMPLETED);

        when(bookingRepository.findById("booking-1"))
                .thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Booking captured = escrowService.capturePayment("booking-1");
        assertThat(captured.getPaymentStatus()).isEqualTo(PaymentStatus.CAPTURED);
    }

    @Test
    void authorizePayment_throwsWhenIntentMissing() {

        when(bookingRepository.findById("booking-1"))
                .thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> escrowService.authorizePayment("booking-1"))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void initiatePayment_throwsWhenBookingMissing() {
        when(bookingRepository.findById("missing"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> escrowService.initiatePayment("missing", 50))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
