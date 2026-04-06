package com.fixlocal.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceOffering {

    private String id;

    private String name;

    private String description;

    private Double basePrice;

    private Integer durationMinutes;
}
