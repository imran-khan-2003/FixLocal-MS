package com.fixlocal.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AuthResponse {

    String token;
    String type; // Bearer
    UserResponseDTO user;
}