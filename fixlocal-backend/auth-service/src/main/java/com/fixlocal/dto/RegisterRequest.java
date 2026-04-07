package com.fixlocal.dto;

import com.fixlocal.enums.Role;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name cannot exceed 100 characters")
    private String name;

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+?[0-9]{7,15}$", message = "Enter a valid phone number")
    private String phone;

    @NotNull(message = "Role is required")
    private Role role;

    // Tradesperson-only fields

    @Size(max = 100, message = "Occupation too long")
    private String occupation;

    @Size(max = 100, message = "Working city too long")
    private String workingCity;

    @Min(value = 0, message = "Experience cannot be negative")
    @Max(value = 60, message = "Experience unrealistic")
    private Integer experience;
}