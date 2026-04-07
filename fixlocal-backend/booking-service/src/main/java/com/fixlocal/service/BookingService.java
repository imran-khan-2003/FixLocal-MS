package com.fixlocal.service;

import com.fixlocal.dto.*;
import com.fixlocal.exception.*;
import com.fixlocal.entity.*;
import com.fixlocal.enums.*;
import com.fixlocal.repository.BookingRepository;
import com.fixlocal.repository.LiveLocationRepository;
import com.fixlocal.repository.UserRepository;
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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

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
