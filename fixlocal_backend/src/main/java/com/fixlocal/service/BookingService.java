package com.fixlocal.service;

import com.fixlocal.dto.*;
import com.fixlocal.exception.*;
import com.fixlocal.model.*;
import com.fixlocal.repository.BookingRepository;
import com.fixlocal.repository.LiveLocationRepository;
import com.fixlocal.repository.UserRepository;
import com.fixlocal.service.NotificationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.*;
import org.springframework.data.mongodb.core.aggregation.*;

import com.mongodb.client.result.UpdateResult;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingService {

    private static final long LIVE_LOCATION_STALE_THRESHOLD_SECONDS = 300;

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final LiveLocationRepository liveLocationRepository;
    private final MongoTemplate mongoTemplate;
    private final NotificationService notificationService;
    private final EscrowService escrowService;

    // ======================================================
    // 🔐 Helper: Get logged-in user (BLOCK SAFE)
    // ======================================================
    private User getLoggedInUser() {

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        if (user.isBlocked()) {
            throw new UnauthorizedException("Your account is blocked");
        }

        return user;
    }

    public Booking initiatePayment(String bookingId, double amount) {
        return escrowService.initiatePayment(bookingId, amount);
    }

    public Booking authorizePayment(String bookingId) {
        return escrowService.authorizePayment(bookingId);
    }

    public Booking capturePayment(String bookingId) {
        return escrowService.capturePayment(bookingId);
    }

    public Booking refundPayment(String bookingId) {
        return escrowService.refundPayment(bookingId);
    }

    // ======================================================
    // ✅ CREATE BOOKING
    // ======================================================
    @Transactional
    public Booking createBooking(BookingRequest request) {

        User user = getLoggedInUser();

        if (user.getRole() != Role.USER) {
            throw new UnauthorizedException("Only users can create bookings");
        }

        String tradespersonId = request.getTradespersonId();

        User tradesperson = userRepository.findById(tradespersonId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Tradesperson not found"));

        if (tradesperson.getRole() != Role.TRADESPERSON) {
            throw new BadRequestException("Selected user is not a tradesperson");
        }

        if (tradesperson.isBlocked()) {
            throw new ConflictException("Tradesperson account is blocked");
        }

        if (tradesperson.getStatus() != Status.AVAILABLE) {
            throw new ConflictException("Tradesperson is currently busy");
        }

        boolean alreadyPending = bookingRepository
                .existsByUserIdAndTradespersonIdAndStatus(
                        user.getId(),
                        tradespersonId,
                        BookingStatus.PENDING
                );

        if (alreadyPending) {
            throw new ConflictException(
                    "You already have a pending booking with this tradesperson"
            );
        }

        Booking booking = Booking.builder()
                .userId(user.getId())
                .tradespersonId(tradespersonId)
                .serviceAddress(request.getServiceAddress())
                .serviceDescription(request.getServiceDescription())
                .userName(user.getName())
                .userPhone(user.getPhone())
                .userCity(request.getUserCity())
                .userLatitude(request.getUserLatitude())
                .userLongitude(request.getUserLongitude())
                .price(request.getOfferAmount())
                .initialOfferAmount(request.getOfferAmount())
                .lastOfferBy(OfferSide.USER)
                .awaitingResponseFrom(OfferSide.TRADESPERSON)
                .status(BookingStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        booking.getOfferHistory().add(
                PriceOffer.builder()
                        .id(UUID.randomUUID().toString())
                        .amount(request.getOfferAmount())
                        .offeredBy(OfferSide.USER)
                        .message("Initial offer")
                        .build()
        );

        log.info("Booking created by user {} for tradesperson {}",
                user.getId(),
                tradespersonId);

        Booking saved = bookingRepository.save(booking);

        notificationService.createNotification(
                tradespersonId,
                "New booking request from " + user.getName(),
                NotificationType.BOOKING_CREATED
        );

        publishBookingEvent(saved,
                NotificationType.BOOKING_CREATED,
                "Booking created");

        return saved;
    }

    @Transactional
    public PriceOffer submitCounterOffer(String bookingId, PriceOfferRequest request) {

        User actor = getLoggedInUser();

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        validateOfferState(booking, actor);

        OfferSide actorSide = actor.getRole() == Role.USER
                ? OfferSide.USER
                : OfferSide.TRADESPERSON;

        if (!actorSide.equals(booking.getAwaitingResponseFrom())) {
            throw new ConflictException("It is not your turn to respond");
        }

        PriceOffer offer = PriceOffer.builder()
                .id(UUID.randomUUID().toString())
                .amount(request.getAmount())
                .message(request.getMessage())
                .offeredBy(actorSide)
                .build();

        booking.getOfferHistory().add(offer);
        booking.setLastOfferBy(actorSide);
        booking.setAwaitingResponseFrom(actorSide == OfferSide.USER
                ? OfferSide.TRADESPERSON
                : OfferSide.USER);
        booking.setPrice(request.getAmount());

        bookingRepository.save(booking);

        notifyOfferUpdate(booking,
                actorSide == OfferSide.USER ? booking.getTradespersonId() : booking.getUserId(),
                NotificationType.OFFER_SUBMITTED,
                "New offer submitted");

        return offer;
    }

    @Transactional
    public Booking acceptOffer(String bookingId, String offerId) {

        User actor = getLoggedInUser();

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        validateOfferState(booking, actor);

        PriceOffer offer = booking.getOfferHistory().stream()
                .filter(o -> o.getId().equals(offerId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Offer not found"));

        OfferSide actorSide = actor.getRole() == Role.USER
                ? OfferSide.USER
                : OfferSide.TRADESPERSON;

        if (offer.getOfferedBy() == actorSide) {
            throw new ConflictException("You cannot accept your own offer");
        }

        offer.setAccepted(true);
        offer.setAcceptedBy(actorSide);
        offer.setAcceptedAt(LocalDateTime.now());
        booking.setAcceptedOfferId(offer.getId());
        booking.setPrice(offer.getAmount());
        booking.setAwaitingResponseFrom(null);

        bookingRepository.save(booking);

        notifyOfferUpdate(booking,
                actorSide == OfferSide.USER ? booking.getTradespersonId() : booking.getUserId(),
                NotificationType.OFFER_ACCEPTED,
                "Offer accepted");

        return booking;
    }

    private void validateOfferState(Booking booking, User actor) {

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new ConflictException("Negotiations only allowed for pending bookings");
        }

        if (!booking.getUserId().equals(actor.getId())
                && !booking.getTradespersonId().equals(actor.getId())) {
            throw new UnauthorizedException("Not authorized for this booking");
        }
    }

    private void notifyOfferUpdate(Booking booking,
                                   String targetUserId,
                                   NotificationType type,
                                   String message) {

        notificationService.createNotification(
                targetUserId,
                message,
                type
        );

        publishBookingEvent(booking, type, message);
    }
    // ======================================================
    // ✅ ACCEPT BOOKING (CONCURRENCY SAFE)
    // ======================================================
    @Transactional
    public Booking acceptBooking(String bookingId) {

        User loggedInUser = getLoggedInUser();

        if (loggedInUser.getRole() != Role.TRADESPERSON) {
            throw new UnauthorizedException("Only tradesperson can accept booking");
        }

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Booking not found"));

        if (!booking.getTradespersonId().equals(loggedInUser.getId())) {
            throw new UnauthorizedException("You are not authorized to accept this booking");
        }

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new BadRequestException("Only pending bookings can be accepted");
        }

        Query query = new Query(
                Criteria.where("_id").is(loggedInUser.getId())
                        .and("status").is(Status.AVAILABLE)
        );

        Update update = new Update().set("status", Status.BUSY);

        UpdateResult result = mongoTemplate.updateFirst(query, update, User.class);

        if (result.getModifiedCount() == 0) {
            throw new ConflictException("You already have an active booking");
        }

        booking.setStatus(BookingStatus.ACCEPTED);
        booking.setAcceptedAt(LocalDateTime.now());

        Booking saved = bookingRepository.save(booking);

        notificationService.createNotification(
                booking.getUserId(),
                "Your booking has been accepted",
                NotificationType.BOOKING_ACCEPTED
        );

        publishBookingEvent(saved,
                NotificationType.BOOKING_ACCEPTED,
                "Booking accepted");

        log.info("Booking {} accepted by tradesperson {}", bookingId, loggedInUser.getId());

        return saved;
    }

    // ======================================================
    // ✅ REJECT BOOKING
    // ======================================================
    @Transactional
    public Booking rejectBooking(String bookingId) {

        User loggedInUser = getLoggedInUser();

        if (loggedInUser.getRole() != Role.TRADESPERSON) {
            throw new UnauthorizedException("Only tradesperson can reject booking");
        }

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Booking not found"));

        if (!booking.getTradespersonId().equals(loggedInUser.getId())) {
            throw new UnauthorizedException("You are not authorized to reject this booking");
        }

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new BadRequestException("Only pending bookings can be rejected");
        }

        booking.setStatus(BookingStatus.REJECTED);

        Booking saved = bookingRepository.save(booking);

        notificationService.createNotification(
                booking.getUserId(),
                "Your booking was rejected",
                NotificationType.BOOKING_REJECTED
        );

        publishBookingEvent(saved,
                NotificationType.BOOKING_REJECTED,
                "Booking rejected");

        log.info("Booking {} rejected by tradesperson {}", bookingId, loggedInUser.getId());

        return saved;
    }

    // ======================================================
    // ✅ COMPLETE BOOKING
    // ======================================================
    @Transactional
    public Booking completeBooking(String bookingId) {

        User loggedInUser = getLoggedInUser();

        if (loggedInUser.getRole() != Role.TRADESPERSON) {
            throw new UnauthorizedException("Only tradesperson can complete booking");
        }

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Booking not found"));

        if (!booking.getTradespersonId().equals(loggedInUser.getId())) {
            throw new UnauthorizedException("You are not authorized to complete this booking");
        }

        if (booking.getStatus() != BookingStatus.ACCEPTED
                && booking.getStatus() != BookingStatus.EN_ROUTE
                && booking.getStatus() != BookingStatus.ARRIVED) {
            throw new BadRequestException("Booking must be in progress to complete");
        }

        booking.setStatus(BookingStatus.COMPLETED);
        booking.setCompletedAt(LocalDateTime.now());

        Query query = new Query(
                Criteria.where("_id").is(loggedInUser.getId())
                        .and("status").in(Status.BUSY, Status.OFFLINE)
        );

        Update update = new Update().set("status", Status.AVAILABLE);

        mongoTemplate.updateFirst(query, update, User.class);

        Booking saved = bookingRepository.save(booking);

        notificationService.createNotification(
                booking.getUserId(),
                "Job completed. Please leave a review",
                NotificationType.BOOKING_COMPLETED
        );

        publishBookingEvent(saved,
                NotificationType.BOOKING_COMPLETED,
                "Booking completed");

        log.info("Booking {} completed by tradesperson {}", bookingId, loggedInUser.getId());

        liveLocationRepository.deleteByBookingId(bookingId);

        return saved;
    }

    // ======================================================
    // ✅ CANCEL BOOKING
    // ======================================================
    @Transactional
    public Booking cancelBooking(String bookingId, String reason) {

        User loggedInUser = getLoggedInUser();

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Booking not found"));

        BookingStatus currentStatus = booking.getStatus();

        if (currentStatus == BookingStatus.COMPLETED ||
                currentStatus == BookingStatus.REJECTED ||
                currentStatus == BookingStatus.CANCELLED) {

            throw new ConflictException("This booking cannot be cancelled");
        }

        if (loggedInUser.getRole() == Role.USER) {

            if (!booking.getUserId().equals(loggedInUser.getId())) {
                throw new UnauthorizedException("You are not authorized to cancel this booking");
            }

            booking.setCancelledBy(CancellationBy.USER);

        } else if (loggedInUser.getRole() == Role.TRADESPERSON) {

            if (!booking.getTradespersonId().equals(loggedInUser.getId())) {
                throw new UnauthorizedException("You are not authorized to cancel this booking");
            }

            booking.setCancelledBy(CancellationBy.TRADESPERSON);

        } else {
            throw new UnauthorizedException("Unauthorized role");
        }

        if (currentStatus == BookingStatus.ACCEPTED ||
                currentStatus == BookingStatus.EN_ROUTE ||
                currentStatus == BookingStatus.ARRIVED) {

            Query query = new Query(
                    Criteria.where("_id").is(booking.getTradespersonId())
                            .and("status").is(Status.BUSY)
            );

            Update update = new Update().set("status", Status.AVAILABLE);

            mongoTemplate.updateFirst(query, update, User.class);
        }

        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancellationReason(reason);
        booking.setCancelledAt(LocalDateTime.now());

        Booking saved = bookingRepository.save(booking);

        notificationService.createNotification(
                booking.getUserId().equals(loggedInUser.getId()) ?
                        booking.getTradespersonId() : booking.getUserId(),
                "Booking was cancelled",
                NotificationType.BOOKING_CANCELLED
        );

        publishBookingEvent(saved,
                NotificationType.BOOKING_CANCELLED,
                "Booking cancelled");

        log.info("Booking {} cancelled by {}", bookingId, loggedInUser.getRole());

        liveLocationRepository.deleteByBookingId(bookingId);

        return saved;
    }

    // ======================================================
    // GET METHODS
    // ======================================================

    public Booking getBookingById(String bookingId) {

        User loggedInUser = getLoggedInUser();

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Booking not found"));

        if (!booking.getUserId().equals(loggedInUser.getId()) &&
                !booking.getTradespersonId().equals(loggedInUser.getId())) {

            throw new UnauthorizedException("You are not authorized to view this booking");
        }

        return booking;
    }

    public Page<Booking> getBookingsByUser(BookingStatus status, int page, int size) {

        User loggedInUser = getLoggedInUser();

        if (loggedInUser.getRole() != Role.USER) {
            throw new UnauthorizedException("Only users can view their bookings");
        }

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("createdAt").descending()
        );

        return (status != null)
                ? bookingRepository.findByUserIdAndStatus(loggedInUser.getId(), status, pageable)
                : bookingRepository.findByUserId(loggedInUser.getId(), pageable);
    }

    public Page<Booking> getBookingsByTradesperson(BookingStatus status, int page, int size) {

        User loggedInUser = getLoggedInUser();

        if (loggedInUser.getRole() != Role.TRADESPERSON) {
            throw new UnauthorizedException("Only tradesperson can view bookings");
        }

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("createdAt").descending()
        );

        return (status != null)
                ? bookingRepository.findByTradespersonIdAndStatus(loggedInUser.getId(), status, pageable)
                : bookingRepository.findByTradespersonId(loggedInUser.getId(), pageable);
    }

    public BookingStatsDTO getBookingStats() {

        User loggedInUser = getLoggedInUser();

        Criteria criteria = (loggedInUser.getRole() == Role.USER)
                ? Criteria.where("userId").is(loggedInUser.getId())
                : Criteria.where("tradespersonId").is(loggedInUser.getId());

        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(criteria),
                Aggregation.group("status").count().as("count")
        );

        AggregationResults<org.bson.Document> results =
                mongoTemplate.aggregate(
                        aggregation,
                        Booking.class,
                        org.bson.Document.class
                );

        long total = 0;
        long pending = 0;
        long accepted = 0;
        long completed = 0;
        long cancelled = 0;
        long rejected = 0;

        for (org.bson.Document doc : results.getMappedResults()) {

            String status = doc.getString("_id");
            long count = ((Number) doc.get("count")).longValue();

            total += count;

            switch (status) {
                case "PENDING":
                    pending = count;
                    break;

                case "ACCEPTED":
                    accepted = count;
                    break;

                case "COMPLETED":
                    completed = count;
                    break;

                case "CANCELLED":
                    cancelled = count;
                    break;

                case "REJECTED":
                    rejected = count;
                    break;
            }
        }

        long active = accepted; // Active bookings = accepted bookings

        return new BookingStatsDTO(
                total,
                pending,
                accepted,
                completed,
                cancelled,
                rejected,
                active
        );
    }

    @Transactional
    public Booking startTrip(String bookingId) {

        User loggedInUser = getLoggedInUser();

        if (loggedInUser.getRole() != Role.TRADESPERSON) {
            throw new UnauthorizedException("Only tradesperson can start trip");
        }

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Booking not found"));

        if (!booking.getTradespersonId().equals(loggedInUser.getId())) {
            throw new UnauthorizedException("Not authorized");
        }

        if (booking.getStatus() != BookingStatus.ACCEPTED) {
            throw new BadRequestException("Trip can start only after acceptance");
        }

        booking.setStatus(BookingStatus.EN_ROUTE);
        booking.setEnRouteAt(LocalDateTime.now());

        userRepository.save(loggedInUser);

        Booking saved = bookingRepository.save(booking);

        notificationService.createNotification(
                booking.getUserId(),
                "Tradesperson is en route",
                NotificationType.BOOKING_EN_ROUTE
        );

        publishBookingEvent(saved,
                NotificationType.BOOKING_EN_ROUTE,
                "Tradesperson en route");

        return saved;
    }

    @Transactional
    public Booking markArrived(String bookingId) {

        User loggedInUser = getLoggedInUser();

        if (loggedInUser.getRole() != Role.TRADESPERSON) {
            throw new UnauthorizedException("Only tradesperson can mark arrival");
        }

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Booking not found"));

        if (!booking.getTradespersonId().equals(loggedInUser.getId())) {
            throw new UnauthorizedException("Not authorized");
        }

        if (booking.getStatus() != BookingStatus.EN_ROUTE) {
            throw new BadRequestException("Arrival requires en-route state");
        }

        booking.setStatus(BookingStatus.ARRIVED);
        booking.setArrivedAt(LocalDateTime.now());

        Booking saved = bookingRepository.save(booking);

        notificationService.createNotification(
                booking.getUserId(),
                "Tradesperson arrived",
                NotificationType.BOOKING_ARRIVED
        );

        publishBookingEvent(saved,
                NotificationType.BOOKING_ARRIVED,
                "Tradesperson arrived");

        return saved;
    }

    @Transactional
    public void updateLiveLocation(String bookingId,
                                   double latitude,
                                   double longitude) {

        User loggedInUser = getLoggedInUser();

        if (loggedInUser.getRole() != Role.TRADESPERSON) {
            throw new UnauthorizedException("Only tradesperson can update location");
        }

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Booking not found"));

        if (!booking.getTradespersonId().equals(loggedInUser.getId())) {
            throw new UnauthorizedException("Not authorized");
        }

        if (booking.getStatus() == BookingStatus.CANCELLED ||
                booking.getStatus() == BookingStatus.REJECTED ||
                booking.getStatus() == BookingStatus.COMPLETED) {
            throw new BadRequestException("Cannot update location for closed booking");
        }

        var liveLocation = liveLocationRepository
                .findByBookingId(bookingId)
                .orElseGet(() -> LiveLocation.builder()
                        .bookingId(bookingId)
                        .tradespersonId(loggedInUser.getId())
                        .build());

        Instant now = Instant.now();

        liveLocation.setLatitude(latitude);
        liveLocation.setLongitude(longitude);
        liveLocation.setUpdatedAt(now);

        liveLocationRepository.save(liveLocation);

        LiveLocationEvent event = LiveLocationEvent.builder()
                .bookingId(bookingId)
                .latitude(latitude)
                .longitude(longitude)
                .updatedAt(now)
                .stale(false)
                .build();

        notificationService.publishLiveLocation(
                "/topic/bookings/" + bookingId + "/location",
                event
        );
    }

    public LiveLocationEvent getLiveLocation(String bookingId) {

        User loggedInUser = getLoggedInUser();

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Booking not found"));

        if (!booking.getUserId().equals(loggedInUser.getId())
                && !booking.getTradespersonId().equals(loggedInUser.getId())) {
            throw new UnauthorizedException("Not authorized to view this booking location");
        }

        LiveLocation liveLocation = liveLocationRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Live location not available"));

        Instant updatedAt = liveLocation.getUpdatedAt();
        Instant now = Instant.now();
        boolean stale = updatedAt == null
                || Duration.between(updatedAt, now).getSeconds() > LIVE_LOCATION_STALE_THRESHOLD_SECONDS;

        return LiveLocationEvent.builder()
                .bookingId(bookingId)
                .latitude(liveLocation.getLatitude())
                .longitude(liveLocation.getLongitude())
                .updatedAt(updatedAt)
                .stale(stale)
                .build();
    }

    private void publishBookingEvent(Booking booking,
                                     NotificationType type,
                                     String message) {

        BookingEventPayload payload = BookingEventPayload.builder()
                .bookingId(booking.getId())
                .status(booking.getStatus())
                .type(type)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();

        notificationService.publishBookingEvent(
                "/topic/bookings/" + booking.getUserId(),
                payload
        );

        notificationService.publishBookingEvent(
                "/topic/bookings/tradesperson/" + booking.getTradespersonId(),
                payload
        );
    }

}