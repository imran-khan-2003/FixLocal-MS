package com.fixlocal.service.impl;

import com.fixlocal.dto.InternalLiveLocationUpsertRequest;
import com.fixlocal.dto.LiveLocationEvent;
import com.fixlocal.entity.LiveLocation;
import com.fixlocal.exception.ErrorCode;
import com.fixlocal.exception.LocationException;
import com.fixlocal.repository.LiveLocationRepository;
import com.fixlocal.service.LocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class LocationServiceImpl implements LocationService {

    private final LiveLocationRepository liveLocationRepository;

    @Value("${location.stale-threshold-seconds:300}")
    private long staleThresholdSeconds;

    @Override
    @Transactional
    public void upsertLocation(String bookingId, InternalLiveLocationUpsertRequest request) {
        if (bookingId == null || bookingId.isBlank()) {
            throw new LocationException(ErrorCode.LOCATION_UPDATE_INVALID, "bookingId is required");
        }

        LiveLocation liveLocation = liveLocationRepository.findByBookingId(bookingId)
                .orElseGet(() -> LiveLocation.builder()
                        .bookingId(bookingId)
                        .build());

        liveLocation.setTradespersonId(request.getTradespersonId());
        liveLocation.setLatitude(request.getLatitude());
        liveLocation.setLongitude(request.getLongitude());
        liveLocation.setUpdatedAt(Instant.now());

        liveLocationRepository.save(liveLocation);
    }

    @Override
    public LiveLocationEvent getLocation(String bookingId) {
        LiveLocation liveLocation = liveLocationRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new LocationException(ErrorCode.LOCATION_NOT_FOUND));

        Instant updatedAt = liveLocation.getUpdatedAt();
        Instant now = Instant.now();
        boolean stale = updatedAt == null
                || Duration.between(updatedAt, now).getSeconds() > staleThresholdSeconds;

        return LiveLocationEvent.builder()
                .bookingId(bookingId)
                .latitude(liveLocation.getLatitude())
                .longitude(liveLocation.getLongitude())
                .updatedAt(updatedAt)
                .stale(stale)
                .build();
    }

    @Override
    @Transactional
    public void deleteLocation(String bookingId) {
        liveLocationRepository.deleteByBookingId(bookingId);
    }
}
