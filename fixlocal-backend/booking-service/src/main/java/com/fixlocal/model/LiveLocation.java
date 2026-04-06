package com.fixlocal.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "live_locations")
@CompoundIndex(name = "booking_idx", def = "{'bookingId': 1}", unique = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LiveLocation {

    @Id
    private String id;

    @Indexed
    private String bookingId;

    @Indexed
    private String tradespersonId;

    private Double latitude;

    private Double longitude;

    @Indexed(name = "live_location_ttl_idx", expireAfterSeconds = 900)
    private Instant updatedAt;
}
