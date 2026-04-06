package com.fixlocal.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InternalUserDTO {
    private String id;
    private String name;
    private String email;
    private String phone;
    private String role;
    private boolean blocked;
    private Double averageRating;
    private Integer totalReviews;
}
