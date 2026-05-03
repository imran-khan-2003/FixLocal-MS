package com.fixlocal.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentVerificationRequest {

    @NotBlank
    private String orderId;

    @NotBlank
    private String paymentId;

    @NotBlank
    private String signature;
}
