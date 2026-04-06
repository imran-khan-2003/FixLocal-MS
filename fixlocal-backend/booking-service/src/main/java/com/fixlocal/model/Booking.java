package com.fixlocal.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "bookings")
@CompoundIndexes({
        @CompoundIndex(name = "user_idx", def = "{'userId': 1}"),
        @CompoundIndex(name = "tradesperson_idx", def = "{'tradespersonId': 1}")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Booking {

    @Id
    private String id;

    @Indexed
    private String userId;

    @Indexed
    private String tradespersonId;

    // 🔥 SERVICE DETAILS
    private String serviceAddress;

    private String serviceDescription;

    // 📍 USER SNAPSHOT (for tradesperson context)
    private String userName;

    private String userPhone;

    private String userCity;

    private Double userLatitude;

    private Double userLongitude;

    private Double price;

    private Double initialOfferAmount;

    private OfferSide lastOfferBy;

    private OfferSide awaitingResponseFrom;

    private String acceptedOfferId;

    @Builder.Default
    private List<PriceOffer> offerHistory = new ArrayList<>();

    private PaymentStatus paymentStatus;

    private String paymentIntentId;

    @Builder.Default
    private BookingStatus status = BookingStatus.PENDING;

    private LocalDateTime acceptedAt;

    private LocalDateTime enRouteAt;

    private LocalDateTime arrivedAt;

    private LocalDateTime completedAt;

    @Builder.Default
    private Boolean reviewSubmitted = false;

    private Integer userRating;

    private LocalDateTime reviewedAt;

    private String reviewId;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    private CancellationBy cancelledBy;

    private String cancellationReason;

    private LocalDateTime cancelledAt;
}