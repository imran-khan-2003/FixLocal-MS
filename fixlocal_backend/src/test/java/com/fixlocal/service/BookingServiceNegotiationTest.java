package com.fixlocal.service;

import com.fixlocal.dto.PriceOfferRequest;
import com.fixlocal.exception.ConflictException;
import com.fixlocal.model.*;
import com.fixlocal.repository.BookingRepository;
import com.fixlocal.repository.LiveLocationRepository;
import com.fixlocal.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceNegotiationTest {

    private static final String USER_EMAIL = "user@mail.com";
    private static final String TRADES_EMAIL = "tp@mail.com";

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private LiveLocationRepository liveLocationRepository;

    @Mock
    private MongoTemplate mongoTemplate;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private BookingService bookingService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void submitCounterOffer_fromTradespersonAppendsOfferAndFlipsTurn() {

        Booking booking = baseBooking();
        booking.setAwaitingResponseFrom(OfferSide.TRADESPERSON);
        booking.setLastOfferBy(OfferSide.USER);

        PriceOffer initialOffer = PriceOffer.builder()
                .id("offer-1")
                .amount(100.0)
                .offeredBy(OfferSide.USER)
                .createdAt(LocalDateTime.now())
                .build();

        booking.setOfferHistory(new ArrayList<>(List.of(initialOffer)));

        mockLoggedInUser(TRADES_EMAIL, Role.TRADESPERSON, booking.getTradespersonId());

        when(bookingRepository.findById(booking.getId()))
                .thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PriceOfferRequest request = new PriceOfferRequest();
        request.setAmount(150.0);
        request.setMessage("Can do for 150");

        PriceOffer offer = bookingService.submitCounterOffer(booking.getId(), request);

        assertThat(offer.getAmount()).isEqualTo(150.0);
        assertThat(offer.getOfferedBy()).isEqualTo(OfferSide.TRADESPERSON);
        assertThat(booking.getOfferHistory()).hasSize(2);
        assertThat(booking.getLastOfferBy()).isEqualTo(OfferSide.TRADESPERSON);
        assertThat(booking.getAwaitingResponseFrom()).isEqualTo(OfferSide.USER);
        assertThat(booking.getPrice()).isEqualTo(150.0);

        verify(notificationService).createNotification(eq(booking.getUserId()), anyString(), eq(NotificationType.OFFER_SUBMITTED));
    }

    @Test
    void acceptOffer_fromUserMarksOfferAccepted() {

        Booking booking = baseBooking();
        booking.setAwaitingResponseFrom(OfferSide.USER);
        booking.setLastOfferBy(OfferSide.TRADESPERSON);

        PriceOffer userOffer = PriceOffer.builder()
                .id("offer-1")
                .amount(100.0)
                .offeredBy(OfferSide.USER)
                .createdAt(LocalDateTime.now().minusMinutes(5))
                .build();

        PriceOffer tradesCounter = PriceOffer.builder()
                .id("offer-2")
                .amount(140.0)
                .offeredBy(OfferSide.TRADESPERSON)
                .createdAt(LocalDateTime.now())
                .build();

        booking.setOfferHistory(new ArrayList<>(List.of(userOffer, tradesCounter)));

        mockLoggedInUser(USER_EMAIL, Role.USER, booking.getUserId());

        when(bookingRepository.findById(booking.getId()))
                .thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Booking updated = bookingService.acceptOffer(booking.getId(), "offer-2");

        assertThat(updated.getAcceptedOfferId()).isEqualTo("offer-2");
        assertThat(updated.getPrice()).isEqualTo(140.0);
        assertThat(tradesCounter.isAccepted()).isTrue();
        assertThat(tradesCounter.getAcceptedBy()).isEqualTo(OfferSide.USER);
        assertThat(updated.getAwaitingResponseFrom()).isNull();

        verify(notificationService).createNotification(eq(booking.getTradespersonId()), anyString(), eq(NotificationType.OFFER_ACCEPTED));
    }

    @Test
    void submitCounterOffer_outOfTurnThrowsConflict() {

        Booking booking = baseBooking();
        booking.setAwaitingResponseFrom(OfferSide.TRADESPERSON);

        mockLoggedInUser(USER_EMAIL, Role.USER, booking.getUserId());

        when(bookingRepository.findById(booking.getId()))
                .thenReturn(Optional.of(booking));

        PriceOfferRequest request = new PriceOfferRequest();
        request.setAmount(120.0);

        assertThrows(ConflictException.class,
                () -> bookingService.submitCounterOffer(booking.getId(), request));
    }

    private Booking baseBooking() {

        return Booking.builder()
                .id("booking-1")
                .userId("user-1")
                .tradespersonId("tp-1")
                .status(BookingStatus.PENDING)
                .offerHistory(new ArrayList<>())
                .build();
    }

    private void mockLoggedInUser(String email, Role role, String domainId) {

        SecurityContext context = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(email);
        when(context.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(context);

        User user = User.builder()
                .id(domainId)
                .email(email)
                .role(role)
                .build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
    }
}
