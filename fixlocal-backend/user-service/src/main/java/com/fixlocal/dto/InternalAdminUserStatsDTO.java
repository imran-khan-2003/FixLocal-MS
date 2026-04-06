package com.fixlocal.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InternalAdminUserStatsDTO {
    private long totalUsers;
    private long totalTradespersons;
    private long pendingVerifications;
    private long blockedAccounts;
    private double averagePlatformRating;
}
