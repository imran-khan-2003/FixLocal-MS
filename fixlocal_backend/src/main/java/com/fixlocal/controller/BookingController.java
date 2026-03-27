package com.fixlocal.controller;

import com.fixlocal.dto.*;
import com.fixlocal.model.Booking;
import com.fixlocal.model.BookingStatus;
import com.fixlocal.model.PriceOffer;
import com.fixlocal.service.BookingService;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
@Validated
public class BookingController {

    private final BookingService bookingService;

    // CREATE
    @PostMapping
    public ResponseEntity<Booking> createBooking(
            @Valid @RequestBody BookingRequest request) {

        Booking booking = bookingService.createBooking(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(booking);
    }

    @PostMapping("/{bookingId}/offers")
    public ResponseEntity<PriceOffer> submitOffer(
            @PathVariable String bookingId,
            @Valid @RequestBody PriceOfferRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(bookingService.submitCounterOffer(bookingId, request));
    }

    @PatchMapping("/{bookingId}/offers/{offerId}/accept")
    public ResponseEntity<Booking> acceptOffer(
            @PathVariable String bookingId,
            @PathVariable String offerId) {

        return ResponseEntity.ok(bookingService.acceptOffer(bookingId, offerId));
    }

    // ACCEPT
    @PatchMapping("/{bookingId}/accept")
    public ResponseEntity<Booking> acceptBooking(@PathVariable String bookingId) {
        return ResponseEntity.ok(bookingService.acceptBooking(bookingId));
    }

    // REJECT
    @PatchMapping("/{bookingId}/reject")
    public ResponseEntity<Booking> rejectBooking(@PathVariable String bookingId) {
        return ResponseEntity.ok(bookingService.rejectBooking(bookingId));
    }

    // COMPLETE
    @PatchMapping("/{bookingId}/complete")
    public ResponseEntity<Booking> completeBooking(@PathVariable String bookingId) {
        return ResponseEntity.ok(bookingService.completeBooking(bookingId));
    }

    @PatchMapping("/{bookingId}/start-trip")
    public ResponseEntity<Booking> startTrip(@PathVariable String bookingId) {
        return ResponseEntity.ok(bookingService.startTrip(bookingId));
    }

    @PatchMapping("/{bookingId}/arrived")
    public ResponseEntity<Booking> markArrived(@PathVariable String bookingId) {
        return ResponseEntity.ok(bookingService.markArrived(bookingId));
    }

    @PostMapping("/{bookingId}/location")
    public ResponseEntity<Void> updateLocation(
            @PathVariable String bookingId,
            @Valid @RequestBody LiveLocationRequest request) {

        bookingService.updateLiveLocation(
                bookingId,
                request.getLatitude(),
                request.getLongitude()
        );

        return ResponseEntity.accepted().build();
    }

    @GetMapping("/{bookingId}/location")
    public ResponseEntity<LiveLocationEvent> getLiveLocation(
            @PathVariable String bookingId) {

        return ResponseEntity.ok(bookingService.getLiveLocation(bookingId));
    }

    // CANCEL
    @PatchMapping("/{bookingId}/cancel")
    public ResponseEntity<Booking> cancelBooking(
            @PathVariable String bookingId,
            @Valid @RequestBody CancelRequest request) {

        return ResponseEntity.ok(
                bookingService.cancelBooking(bookingId, request.getReason()));
    }

    // GET SINGLE
    @GetMapping("/{bookingId}")
    public ResponseEntity<Booking> getBooking(@PathVariable String bookingId) {
        return ResponseEntity.ok(bookingService.getBookingById(bookingId));
    }

    // USER BOOKINGS
    @GetMapping("/user")
    public ResponseEntity<Page<Booking>> getUserBookings(
            @RequestParam(required = false) BookingStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(
                bookingService.getBookingsByUser(status, page, size));
    }

    // TRADESPERSON BOOKINGS
    @GetMapping("/tradesperson")
    public ResponseEntity<Page<Booking>> getTradespersonBookings(
            @RequestParam(required = false) BookingStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(
                bookingService.getBookingsByTradesperson(status, page, size));
    }

    // STATS
    @GetMapping("/stats")
    public ResponseEntity<BookingStatsDTO> getBookingStats() {
        return ResponseEntity.ok(bookingService.getBookingStats());
    }

    @PostMapping("/{bookingId}/payments/initiate")
    public ResponseEntity<Booking> initiatePayment(
            @PathVariable String bookingId,
            @RequestParam double amount) {
        return ResponseEntity.ok(bookingService.initiatePayment(bookingId, amount));
    }

    @PostMapping("/{bookingId}/payments/authorize")
    public ResponseEntity<Booking> authorizePayment(
            @PathVariable String bookingId) {
        return ResponseEntity.ok(bookingService.authorizePayment(bookingId));
    }

    @PostMapping("/{bookingId}/payments/capture")
    public ResponseEntity<Booking> capturePayment(
            @PathVariable String bookingId) {
        return ResponseEntity.ok(bookingService.capturePayment(bookingId));
    }

    @PostMapping("/{bookingId}/payments/refund")
    public ResponseEntity<Booking> refundPayment(
            @PathVariable String bookingId) {
        return ResponseEntity.ok(bookingService.refundPayment(bookingId));
    }
}