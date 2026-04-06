package com.fixlocal.dto;

import lombok.Data;

@Data
public class ServiceOfferingDTO {

    private String id;
    private String name;
    private String description;
    private Double basePrice;
    private Integer durationMinutes;
}
