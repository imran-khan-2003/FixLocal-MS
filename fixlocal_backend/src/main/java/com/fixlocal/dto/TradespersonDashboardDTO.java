package com.fixlocal.dto;

import com.fixlocal.model.BookingStatus;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class TradespersonDashboardDTO {

    private UserResponseDTO profile;
    private long pendingRequests;
    private long activeBookings;
    private long completedBookings;
    private long totalBookings;
    private double averageRating;
    private int totalReviews;
    private Map<BookingStatus, Long> bookingBreakdown;
}
