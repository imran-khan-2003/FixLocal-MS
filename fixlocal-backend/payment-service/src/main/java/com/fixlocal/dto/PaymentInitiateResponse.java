package com.fixlocal.dto;

import com.fixlocal.entity.Booking;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentInitiateResponse {
    private Booking booking;
    private String orderId;
    private String keyId;
    private String currency;
    private long amount;
}
