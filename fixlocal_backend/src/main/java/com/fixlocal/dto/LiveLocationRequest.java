package com.fixlocal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LiveLocationRequest {

    @NotBlank
    private String bookingId;

    @NotNull
    private Double latitude;

    @NotNull
    private Double longitude;
}
