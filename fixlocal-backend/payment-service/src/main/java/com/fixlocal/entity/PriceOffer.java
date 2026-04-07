package com.fixlocal.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.fixlocal.enums.OfferSide;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriceOffer {

    private String id;

    private Double amount;

    private OfferSide offeredBy;

    private String message;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    private boolean accepted;

    private OfferSide acceptedBy;

    private LocalDateTime acceptedAt;
}
