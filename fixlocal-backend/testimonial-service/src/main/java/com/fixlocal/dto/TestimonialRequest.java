package com.fixlocal.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TestimonialRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String city;

    @NotBlank
    private String role;

    @NotBlank
    private String quote;
}