package com.fixlocal.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class PriceOfferRequest {

    @NotNull(message = "Offer amount is required")
    @Positive(message = "Offer amount must be positive")
    private Double amount;

    private String message;
}
