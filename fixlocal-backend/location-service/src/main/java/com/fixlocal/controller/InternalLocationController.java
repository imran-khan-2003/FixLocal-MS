package com.fixlocal.controller;

import com.fixlocal.dto.InternalLiveLocationUpsertRequest;
import com.fixlocal.dto.LiveLocationEvent;
import com.fixlocal.service.LocationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/location")
@RequiredArgsConstructor
@Validated
public class InternalLocationController {

    private final LocationService locationService;

    @PutMapping("/bookings/{bookingId}")
    public ResponseEntity<Void> upsertLocation(
            @PathVariable String bookingId,
            @Valid @RequestBody InternalLiveLocationUpsertRequest request
    ) {
        locationService.upsertLocation(bookingId, request);
        return ResponseEntity.accepted().build();
    }

    @GetMapping("/bookings/{bookingId}")
    public ResponseEntity<LiveLocationEvent> getLocation(@PathVariable String bookingId) {
        return ResponseEntity.ok(locationService.getLocation(bookingId));
    }

    @DeleteMapping("/bookings/{bookingId}")
    public ResponseEntity<Void> deleteLocation(@PathVariable String bookingId) {
        locationService.deleteLocation(bookingId);
        return ResponseEntity.noContent().build();
    }
}
