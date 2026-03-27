package com.fixlocal.dto;

import lombok.Data;

@Data
public class AdminStatsDTO {

    private long totalUsers;
    private long totalTradespersons;
    private long totalBookings;

    private long completedBookings;
    private long pendingBookings;
    private long cancelledBookings;

    private long rejectedBookings;

    private double averagePlatformRating;

    private long activeConversations;

    private long pendingVerifications;

    private long blockedAccounts;
}