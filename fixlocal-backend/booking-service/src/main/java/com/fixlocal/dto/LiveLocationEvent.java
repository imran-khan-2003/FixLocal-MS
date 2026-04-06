package com.fixlocal.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class LiveLocationEvent {

    private String bookingId;
    private Double latitude;
    private Double longitude;
    private Instant updatedAt;

    private boolean stale;
}
