package com.fixlocal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingStatsDTO {

    private long total;
    private long pending;
    private long accepted;
    private long completed;
    private long cancelled;
    private long rejected;
    private long active;
}