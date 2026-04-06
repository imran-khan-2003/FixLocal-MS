package com.fixlocal.dto;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DisputeRequest {

    @NotBlank(message = "Booking ID is required")
    private String bookingId;

    private String reporterId;

    @NotBlank(message = "Reason is required")
    private String reason;

    private String desiredOutcome;
}