package com.fixlocal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CancelRequest {

    @NotBlank(message = "Cancellation reason is required")
    @Size(max = 300, message = "Reason cannot exceed 300 characters")
    private String reason;
}