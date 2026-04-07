package com.fixlocal.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.*;
import org.springframework.data.mongodb.core.mapping.Document;
import com.fasterxml.jackson.annotation.JsonIgnore;

import com.fixlocal.enums.Role;
import com.fixlocal.enums.Status;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "users")

@CompoundIndexes({
        @CompoundIndex(name = "city_occupation_idx", def = "{'workingCity': 1, 'occupation': 1}"),
        @CompoundIndex(name = "role_city_idx", def = "{'role': 1, 'workingCity': 1}")
})

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    private String id;

    private String name;

    @Indexed(unique = true)
    private String email;

    @JsonIgnore
    private String password;

    @Indexed
    private Role role;

    // searchable text field
    @TextIndexed
    private String occupation;

    @Indexed
    private String workingCity;

    // years of experience
    @Builder.Default
    private Integer experience = 0;

    // ⭐ Worker profile fields (NEW)
    private String profileImage;

    private String bio;

    private String phone;

    @Builder.Default
    private Integer completedJobs = 0;

    // worker availability
    @Builder.Default
    private boolean available = true;

    @Builder.Default
    private Status status = Status.AVAILABLE;

    // active booking reference for tradesperson dashboard
    private String currentBookingId;

    // last known geo coordinates (optional for users, critical for tradespersons)
    private Double lastKnownLatitude;

    private Double lastKnownLongitude;

    // ratings
    @Builder.Default
    private Double averageRating = 0.0;

    @Builder.Default
    private Integer totalReviews = 0;

    @Builder.Default
    private List<String> skillTags = new ArrayList<>();

    @Builder.Default
    private List<ServiceOffering> serviceOfferings = new ArrayList<>();

    // moderation flags
    @Builder.Default
    private boolean blocked = false;

    @Builder.Default
    private boolean verified = true;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
