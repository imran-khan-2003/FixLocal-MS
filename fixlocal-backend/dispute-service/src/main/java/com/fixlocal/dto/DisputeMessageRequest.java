package com.fixlocal.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DisputeMessageRequest {

    private String senderId;

    @NotBlank(message = "Message cannot be empty")
    private String message;
}