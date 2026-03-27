package com.fixlocal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class BookingRequest {

    @NotBlank(message = "Tradesperson ID is required")
    private String tradespersonId;

    @NotBlank(message = "Service address is required")
    private String serviceAddress;

    private String serviceDescription;

    @Positive(message = "Offered price must be positive")
    private Double offerAmount;

    @NotBlank(message = "User city is required")
    private String userCity;

    @NotNull(message = "User latitude is required")
    private Double userLatitude;

    @NotNull(message = "User longitude is required")
    private Double userLongitude;
}