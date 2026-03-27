package com.fixlocal.dto;

import com.fixlocal.model.BookingStatus;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class UserDashboardDTO {

    private UserResponseDTO profile;
    private long upcomingBookings;
    private long activeBookings;
    private long completedBookings;
    private long totalBookings;
    private Map<BookingStatus, Long> bookingBreakdown;
}
