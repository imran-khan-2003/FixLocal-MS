package com.fixlocal.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "reviews")
@CompoundIndexes({
        // ensures only one review per booking
        @CompoundIndex(name = "booking_unique", def = "{'bookingId': 1}", unique = true),

        // fast lookup of tradesperson reviews
        @CompoundIndex(name = "tradesperson_idx", def = "{'tradespersonId': 1}")
})

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Review {

    @Id
    private String id;

    // NOT unique here (unique already handled by compound index above)
    @Indexed
    private String bookingId;

    @Indexed
    private String userId;

    @Indexed
    private String tradespersonId;

    // rating from 1 to 5
    private int rating;

    private String comment;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}