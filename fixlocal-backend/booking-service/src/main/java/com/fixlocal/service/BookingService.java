package com.fixlocal.service;

import com.fixlocal.dto.*;
import com.fixlocal.entity.Booking;
import com.fixlocal.entity.PriceOffer;
import com.fixlocal.enums.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;

public interface BookingService {
    public Booking createBooking(BookingRequest request);
    public PriceOffer submitCounterOffer(String bookingId, PriceOfferRequest request);
    public Booking acceptOffer(String bookingId, String offerId);
    public Booking acceptBooking(String bookingId);
    public Booking rejectBooking(String bookingId);
    public Booking completeBooking(String bookingId);
    public Booking cancelBooking(String bookingId, String reason);
    public Booking getBookingById(String bookingId);
    public Page<Booking> getBookingsByUser(BookingStatus status, int page, int size);
    public Page<Booking> getBookingsByTradesperson(BookingStatus status, int page, int size);
    public BookingStatsDTO getBookingStats();
    public Booking startTrip(String bookingId);
    public Booking markArrived(String bookingId);
    public void updateLiveLocation(String bookingId, double latitude, double longitude);
    public LiveLocationEvent getLiveLocation(String bookingId);
    public InternalBookingDTO getInternalBookingById(String bookingId);
    public Page<InternalBookingDTO> getAdminBookings(Pageable pageable);
    public InternalAdminBookingStatsDTO getAdminBookingStats();
    public Map<String, Long> getUserStatusCounts(String userId);
    public Map<String, Long> getTradespersonStatusCounts(String tradespersonId);
    public void applyReviewUpdate(String bookingId, InternalReviewUpdateRequest request);
}
