package com.fixlocal.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InternalAdminBookingStatsDTO {
    private long totalBookings;
    private long completedBookings;
    private long pendingBookings;
    private long cancelledBookings;
    private long rejectedBookings;
}
